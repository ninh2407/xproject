package vn.tienlo.api.security.app;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.security.api.SessionRevocationGate;
import vn.tienlo.api.security.persistence.LoginSessionRepository;
import vn.tienlo.api.security.persistence.SessionRevocationOutboxRepository;

@Service
public class AccountSessionRevocationService implements SessionRevocationGate {
    private final LoginSessionRepository sessionRepository;
    private final SessionRevocationOutboxRepository revocationOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UuidV7Generator uuidV7Generator;
    private final Clock clock;

    public AccountSessionRevocationService(
            LoginSessionRepository sessionRepository,
            SessionRevocationOutboxRepository revocationOutboxRepository,
            ApplicationEventPublisher eventPublisher,
            UuidV7Generator uuidV7Generator,
            Clock clock) {
        this.sessionRepository = sessionRepository;
        this.revocationOutboxRepository = revocationOutboxRepository;
        this.eventPublisher = eventPublisher;
        this.uuidV7Generator = uuidV7Generator;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void revokeAndDeleteAll(UUID accountId) {
        List<UUID> sessionIds = sessionRepository.revokeAll(accountId);
        if (!sessionIds.isEmpty()) {
            UUID eventId = uuidV7Generator.next();
            var occurredAt = clock.instant();
            revocationOutboxRepository.add(eventId, accountId, occurredAt, sessionIds);
            eventPublisher.publishEvent(new SessionRevokedEvent(eventId, accountId, sessionIds));
        }
        sessionRepository.deleteAllForAccount(accountId);
    }
}
