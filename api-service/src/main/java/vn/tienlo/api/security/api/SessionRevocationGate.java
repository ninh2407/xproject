package vn.tienlo.api.security.api;

import java.util.UUID;

/** Security-owned session invalidation used by account deletion (DB-R-100). */
public interface SessionRevocationGate {
    void revokeAndDeleteAll(UUID accountId);
}
