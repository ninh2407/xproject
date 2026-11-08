package vn.tienlo.api.security.api;

public interface RequestFingerprint {
    byte[] hash(String endpoint, String... values);
}
