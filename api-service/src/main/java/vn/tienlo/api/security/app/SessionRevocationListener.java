package vn.tienlo.api.security.app;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SessionRevocationListener {
    private final SessionRevocationOutboxRelay outboxRelay;

    public SessionRevocationListener(SessionRevocationOutboxRelay outboxRelay) {
        this.outboxRelay = outboxRelay;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void revoke(SessionRevokedEvent event) {
        outboxRelay.relay(event.eventId());
    }
}
