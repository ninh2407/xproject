package vn.tienlo.api.server.app;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.server.persistence.ServerRegistryRepository;

@Service
@Transactional(readOnly = true)
public class ServerCatalogService {
    private final ServerRegistryRepository serverRegistryRepository;

    public ServerCatalogService(ServerRegistryRepository serverRegistryRepository) {
        this.serverRegistryRepository = serverRegistryRepository;
    }

    public List<ServerView> listAvailableServers() {
        return serverRegistryRepository.findAllByStatusInOrderByServerIdAsc(List.of("OPEN", "MAINTENANCE")).stream()
                .map(server -> new ServerView(server.getServerId(), server.getNameKey(), server.getStatus()))
                .toList();
    }

    public record ServerView(Short serverId, String nameKey, String status) {}
}
