package vn.tienlo.api.security.app;

import java.util.List;
import java.util.UUID;

public record SessionRevokedEvent(UUID eventId, UUID accountId, List<UUID> sessionIds) {
    public SessionRevokedEvent {
        sessionIds = List.copyOf(sessionIds);
    }
}
