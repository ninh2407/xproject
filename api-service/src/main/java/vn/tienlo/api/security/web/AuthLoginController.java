package vn.tienlo.api.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tienlo.api.generated.publicapi.model.LoginRequest;
import vn.tienlo.api.security.app.LoginResult;
import vn.tienlo.api.security.app.LoginService;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthLoginController {
    private final LoginService loginService;

    public AuthLoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader("X-Request-Id") UUID requestId,
            @RequestHeader("traceparent") @NotBlank String traceparent,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 80) String idempotencyKey,
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest servletRequest) {
        LoginResult result = loginService.login(
                loginRequest,
                idempotencyKey,
                requestId,
                servletRequest.getRemoteAddr());
        return ResponseEntity.status(result.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(result.body());
    }
}
