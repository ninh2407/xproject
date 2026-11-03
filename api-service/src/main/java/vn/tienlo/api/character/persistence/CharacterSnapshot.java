package vn.tienlo.api.character.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CharacterSnapshot(
        UUID characterId,
        String mapId,
        Short channel,
        BigDecimal posX,
        BigDecimal posZ,
        Short facing,
        Integer hp,
        Integer mp,
        long sequence,
        Instant capturedAt) {}
