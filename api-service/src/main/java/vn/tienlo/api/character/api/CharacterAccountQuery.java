package vn.tienlo.api.character.api;

import java.util.List;
import java.util.UUID;

/** Read-only account character summaries exposed to the account module. */
public interface CharacterAccountQuery {
    List<CharacterSummaryView> listByAccountId(UUID accountId);

    record CharacterSummaryView(
            UUID id,
            String name,
            Short serverId,
            Integer generation,
            String backgroundId,
            String lifeState,
            String realm,
            Integer realmStage,
            String endCause) {}
}
