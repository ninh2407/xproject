package vn.tienlo.api.security.app;

public record LoginAttemptOutcomeEvent(String normalizedEmail, String remoteIp, boolean succeeded) {}
