package vn.tienlo.api.security;

import org.springframework.security.oauth2.jwt.JwtException;

public class JwtDependencyUnavailableException extends JwtException {
    public JwtDependencyUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
