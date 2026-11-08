package vn.tienlo.api.server.web;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vn.tienlo.api.generated.publicapi.ServerApi;
import vn.tienlo.api.generated.publicapi.model.ServerCatalog;
import vn.tienlo.api.generated.publicapi.model.ServerSummary;
import vn.tienlo.api.server.app.ServerCatalogService;

@RestController
@Validated
public class ServerController implements ServerApi {
    private final ServerCatalogService serverCatalogService;

    public ServerController(ServerCatalogService serverCatalogService) {
        this.serverCatalogService = serverCatalogService;
    }

    @Override
    public ResponseEntity<ServerCatalog> serverListServers(UUID xRequestId, String traceparent) {
        List<ServerSummary> servers = serverCatalogService
                .listAvailableServers()
                .stream()
                .map(server -> new ServerSummary(
                        server.serverId().intValue(),
                        server.nameKey(),
                        ServerSummary.StatusEnum.fromValue(server.status())))
                .toList();
        return ResponseEntity.ok(new ServerCatalog().servers(servers));
    }
}
