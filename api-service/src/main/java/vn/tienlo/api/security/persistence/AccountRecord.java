package vn.tienlo.api.security.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountRecord(
        UUID id,
        String passwordHash,
        String status,
        Instant suspendedUntil,
        List<String> roles) {}
