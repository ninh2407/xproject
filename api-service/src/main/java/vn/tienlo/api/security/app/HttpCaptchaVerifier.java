package vn.tienlo.api.security.app;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import vn.tienlo.api.security.CaptchaProperties;

@Component
public class HttpCaptchaVerifier implements CaptchaVerifier {
    private final CaptchaProperties properties;
    private final RestClient restClient;

    public HttpCaptchaVerifier(CaptchaProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(2));
        requestFactory.setReadTimeout(java.time.Duration.ofSeconds(3));
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    @Override
    public boolean verify(String token, String remoteIp) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (properties.verificationUri() == null
                || !properties.verificationUri().isAbsolute()
                || !"https".equalsIgnoreCase(properties.verificationUri().getScheme())
                || properties.secret() == null
                || properties.secret().isBlank()) {
            throw new CaptchaUnavailableException("CAPTCHA verification is not configured");
        }

        try {
            CaptchaResponse response = restClient.post()
                    .uri(properties.verificationUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CaptchaRequest(properties.secret(), token, remoteIp))
                    .retrieve()
                    .body(CaptchaResponse.class);
            if (response == null) {
                throw new CaptchaUnavailableException("CAPTCHA verifier returned an empty response");
            }
            return response.success();
        } catch (RestClientException exception) {
            throw new CaptchaUnavailableException("CAPTCHA verifier is unavailable", exception);
        }
    }

    private record CaptchaRequest(String secret, String response, String remoteIp) {}

    private record CaptchaResponse(boolean success) {}
}
