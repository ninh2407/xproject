package vn.tienlo.api.security.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vn.tienlo.api.generated.publicapi.AuthApi;
import vn.tienlo.api.generated.publicapi.model.AccountCreated;
import vn.tienlo.api.generated.publicapi.model.AuthTokens;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.generated.publicapi.model.LoginRequest;
import vn.tienlo.api.generated.publicapi.model.RegisterRequest;
import vn.tienlo.api.security.app.AuthFailureException;
import vn.tienlo.api.security.app.AuthRateLimitException;
import vn.tienlo.api.security.app.AuthRateLimitService;
import vn.tienlo.api.security.app.AuthSessionTokens;
import vn.tienlo.api.security.app.AuthService;
import vn.tienlo.api.security.app.CaptchaVerifier;
import vn.tienlo.api.security.app.LoginResult;
import vn.tienlo.api.security.app.LoginService;

@RestController
@Validated
public class AuthLoginController implements AuthApi {
    static final String REFRESH_COOKIE = "__Secure-xproject_refresh";
    private static final String REFRESH_COOKIE_PATH = "/v1/auth";

    private final LoginService loginService;
    private final AuthService authService;
    private final AuthRateLimitService rateLimitService;
    private final CaptchaVerifier captchaVerifier;

    public AuthLoginController(
            LoginService loginService,
            AuthService authService,
            AuthRateLimitService rateLimitService,
            CaptchaVerifier captchaVerifier) {
        this.loginService = loginService;
        this.authService = authService;
        this.rateLimitService = rateLimitService;
        this.captchaVerifier = captchaVerifier;
    }

    @Override
    public ResponseEntity<AuthTokens> authLogin(
            UUID xRequestId,
            String traceparent,
            String idempotencyKey,
            LoginRequest loginRequest) {
        String remoteIp = remoteIp();
        rateLimitService.consumeRequest(remoteIp);
        String normalizedEmail = loginRequest.getEmail().toLowerCase(Locale.ROOT);
        rateLimitService.assertLoginAllowed(normalizedEmail);
        if (rateLimitService.requiresCaptcha(normalizedEmail)
                && !captchaVerifier.verify(loginRequest.getCaptchaToken(), remoteIp)) {
            throw new AuthRateLimitException(60);
        }

        LoginResult result = loginService.login(loginRequest, idempotencyKey, xRequestId, remoteIp);
        AuthSessionTokens tokens = authSessionTokens(result);
        return ResponseEntity.status(result.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken(), Duration.ofDays(30)).toString())
                .body(publicTokens(tokens));
    }

    @Override
    public ResponseEntity<AccountCreated> authRegister(
            UUID xRequestId,
            String traceparent,
            String idempotencyKey,
            RegisterRequest registerRequest) {
        String remoteIp = remoteIp();
        rateLimitService.consumeRequest(remoteIp);
        if (!captchaVerifier.verify(registerRequest.getCaptchaToken(), remoteIp)) {
            throw new AuthRateLimitException(60);
        }
        LoginResult result = authService.register(registerRequest, idempotencyKey, xRequestId);
        return ResponseEntity.status(result.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(successBody(result, AccountCreated.class));
    }

    @Override
    public ResponseEntity<AuthTokens> authRefresh(UUID xRequestId, String traceparent, String idempotencyKey) {
        String remoteIp = remoteIp();
        rateLimitService.consumeRequest(remoteIp);
        LoginResult result = authService.refresh(
                refreshTokenCookie(),
                idempotencyKey,
                xRequestId,
                remoteIp);
        AuthSessionTokens tokens = authSessionTokens(result);
        return ResponseEntity.status(result.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken(), Duration.ofDays(30)).toString())
                .body(publicTokens(tokens));
    }

    @Override
    public ResponseEntity<Void> authLogout(UUID xRequestId, String traceparent, String idempotencyKey) {
        SessionPrincipal principal = sessionPrincipal(xRequestId);
        LoginResult result = authService.logout(
                refreshTokenCookie(),
                idempotencyKey,
                xRequestId,
                principal.accountId(),
                principal.sessionId());
        requireNoContent(result);
        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie())
                .build();
    }

    @Override
    public ResponseEntity<Void> authLogoutAll(UUID xRequestId, String traceparent, String idempotencyKey) {
        SessionPrincipal principal = sessionPrincipal(xRequestId);
        LoginResult result = authService.logoutAll(
                idempotencyKey,
                xRequestId,
                principal.accountId(),
                principal.sessionId());
        requireNoContent(result);
        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie())
                .build();
    }

    private static String remoteIp() {
        return currentRequest().getRemoteAddr();
    }

    private static String refreshTokenCookie() {
        Cookie[] cookies = currentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    static String expiredRefreshCookie() {
        return refreshCookie("", Duration.ZERO).toString();
    }

    private static ResponseCookie refreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    private static SessionPrincipal sessionPrincipal(UUID requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            try {
                return new SessionPrincipal(
                        UUID.fromString(jwtAuthentication.getToken().getSubject()),
                        UUID.fromString(jwtAuthentication.getToken().getClaimAsString("sid")));
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw unauthenticated(requestId);
            }
        }
        throw unauthenticated(requestId);
    }

    private static AuthSessionTokens authSessionTokens(LoginResult result) {
        if (result.httpStatus() < 200 || result.httpStatus() >= 300) {
            throw new AuthFailureException(result.httpStatus(), (ErrorResponse) result.body());
        }
        return AuthSessionTokens.class.cast(result.body());
    }

    private static AuthTokens publicTokens(AuthSessionTokens tokens) {
        return new AuthTokens(tokens.accessToken(), AuthTokens.ExpiresInEnum.NUMBER_900);
    }

    private static <T> T successBody(LoginResult result, Class<T> bodyType) {
        if (result.httpStatus() < 200 || result.httpStatus() >= 300) {
            throw new AuthFailureException(result.httpStatus(), (ErrorResponse) result.body());
        }
        return bodyType.cast(result.body());
    }

    private static void requireNoContent(LoginResult result) {
        if (result.httpStatus() != 204) {
            throw new AuthFailureException(result.httpStatus(), (ErrorResponse) result.body());
        }
    }

    private static AuthFailureException unauthenticated(UUID requestId) {
        return new AuthFailureException(
                401,
                new ErrorResponse(
                        new ErrorResponseError(
                                "UNAUTHENTICATED",
                                "Authentication is required",
                                "err.unauthenticated"),
                        requestId));
    }

    private record SessionPrincipal(UUID accountId, UUID sessionId) {}
}
