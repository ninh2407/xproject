package vn.tienlo.api.character.app;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.character.api.CharacterAccountQuery;
import vn.tienlo.api.character.api.CharacterAccountQuery.CharacterSummaryView;
import vn.tienlo.api.character.persistence.CharacterRepository;

@Service
@Transactional(readOnly = true)
public class CharacterAccountQueryService implements CharacterAccountQuery {
    private final CharacterRepository characterRepository;

    public CharacterAccountQueryService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    public List<CharacterSummaryView> listByAccountId(UUID accountId) {
        return characterRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(character -> new CharacterSummaryView(
                        character.getId(),
                        character.getName(),
                        character.getServerId(),
                        character.getGeneration(),
                        character.getBackgroundId(),
                        character.getLifeState(),
                        character.getRealm(),
                        character.getRealmStage(),
                        character.getEndCause()))
                .toList();
    }
}
