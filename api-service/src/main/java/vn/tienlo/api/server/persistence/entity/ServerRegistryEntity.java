package vn.tienlo.api.server.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "server_registry", schema = "game")
public class ServerRegistryEntity {
    @Id
    @Column(name = "server_id", nullable = false)
    private Short serverId;

    @Column(name = "name_key", nullable = false, length = 64)
    private String nameKey;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    public ServerRegistryEntity() {}

    public Short getServerId() {
        return serverId;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getStatus() {
        return status;
    }
}
