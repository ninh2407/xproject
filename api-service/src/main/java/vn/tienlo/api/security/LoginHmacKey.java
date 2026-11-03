package vn.tienlo.api.security;

public final class LoginHmacKey {
    private final byte[] value;

    public LoginHmacKey(byte[] value) {
        this.value = value.clone();
    }

    public byte[] value() {
        return value.clone();
    }
}
