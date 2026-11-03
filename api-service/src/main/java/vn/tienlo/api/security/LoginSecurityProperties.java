package vn.tienlo.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.login")
public record LoginSecurityProperties(String idempotencyHmacKey) {}
