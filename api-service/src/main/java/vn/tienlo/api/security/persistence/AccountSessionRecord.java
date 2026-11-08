package vn.tienlo.api.security.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountSessionRecord(
        UUID sessionId,
        UUID accountId,
        String accountStatus,
        Instant suspendedUntil,
        List<String> roles,
        String device,
        Instant expiresAt,
        Instant revokedAt,
        UUID replacedBySessionId) {}
