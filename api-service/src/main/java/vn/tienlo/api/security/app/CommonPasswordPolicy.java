package vn.tienlo.api.security.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CommonPasswordPolicy {
    private final Set<String> commonPasswords;

    public CommonPasswordPolicy(@Value("classpath:security/common-passwords.txt") Resource source) {
        try (var reader = new InputStreamReader(source.getInputStream(), StandardCharsets.UTF_8);
                var buffered = new java.io.BufferedReader(reader)) {
            commonPasswords = buffered.lines()
                    .map(line -> line.strip().toLowerCase(Locale.ROOT))
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the offline common-password list", exception);
        }
    }

    public boolean isAcceptable(String password) {
        return password != null && !commonPasswords.contains(password.toLowerCase(Locale.ROOT));
    }
}
