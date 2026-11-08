package vn.tienlo.api.security.app;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LoginAttemptOutcomeListener {
    private final AuthRateLimitService rateLimitService;

    public LoginAttemptOutcomeListener(AuthRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void record(LoginAttemptOutcomeEvent event) {
        rateLimitService.recordLoginOutcome(event.normalizedEmail(), event.remoteIp(), event.succeeded());
    }
}
