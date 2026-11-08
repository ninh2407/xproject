package vn.tienlo.api.security.app;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.tienlo.api.security.persistence.LoginAttemptRecorder;

@Component
public class SecurityRetentionJob {
    private final LoginAttemptRecorder loginAttemptRecorder;

    public SecurityRetentionJob(LoginAttemptRecorder loginAttemptRecorder) {
        this.loginAttemptRecorder = loginAttemptRecorder;
    }

    @Scheduled(fixedDelay = 3_600_000L)
    public void deleteExpiredLoginAttempts() {
        loginAttemptRecorder.deleteExpiredBatch();
    }
}
