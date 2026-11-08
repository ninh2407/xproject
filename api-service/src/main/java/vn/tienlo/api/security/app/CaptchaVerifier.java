package vn.tienlo.api.security.app;

public interface CaptchaVerifier {
    boolean verify(String token, String remoteIp);
}
