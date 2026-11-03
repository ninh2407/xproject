package vn.tienlo.api.beast.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.beast_pity}; DDL: docs/design/26-spirit-beasts.md. */
@Entity
@Table(name = "beast_pity", schema = "game")
public class BeastPityEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "breed_no_mutation", nullable = false)
    private Short breedNoMutation;

    public BeastPityEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Short getBreedNoMutation() {
        return breedNoMutation;
    }

    public void setBreedNoMutation(Short breedNoMutation) {
        this.breedNoMutation = breedNoMutation;
    }

}
