package vn.tienlo.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtKeyProperties(String keyId, String privateKey, String publicKey) {}
