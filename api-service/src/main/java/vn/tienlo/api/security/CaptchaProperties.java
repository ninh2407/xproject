package vn.tienlo.api.security;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.captcha")
public record CaptchaProperties(URI verificationUri, String secret) {}
