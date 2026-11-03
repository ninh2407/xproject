package vn.tienlo.api.craft.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.profession_memory}; DDL: docs/design/16-alchemy.md. */
@Entity
@Table(name = "profession_memory", schema = "game")
@IdClass(ProfessionMemoryEntity.Key.class)
public class ProfessionMemoryEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "kind", nullable = false, length = 16)
    @Id
    private String kind;

    @Column(name = "max_level", nullable = false)
    private Short maxLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipe_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode recipeIds;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProfessionMemoryEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Short getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Short maxLevel) {
        this.maxLevel = maxLevel;
    }

    public JsonNode getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(JsonNode recipeIds) {
        this.recipeIds = recipeIds;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String kind;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ProfessionMemoryEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(kind, that.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, kind);
        }
    }
}
