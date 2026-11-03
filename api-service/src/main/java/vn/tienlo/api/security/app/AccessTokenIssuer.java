package vn.tienlo.api.security.app;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import vn.tienlo.api.security.JwtKeyProperties;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenIssuer {
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    private final OctetKeyPair signingKey;
    private final JwtKeyProperties jwtKeyProperties;
    private final Clock clock;

    public AccessTokenIssuer(OctetKeyPair signingKey, JwtKeyProperties jwtKeyProperties, Clock clock) {
        this.signingKey = signingKey;
        this.jwtKeyProperties = jwtKeyProperties;
        this.clock = clock;
    }

    /** API-R-030: issue a 15-minute Ed25519 access token and its required claims. */
    public String issue(UUID accountId, UUID sessionId, List<String> roles) {
        Instant issuedAt = clock.instant();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(accountId.toString())
                .claim("sid", sessionId.toString())
                .claim("roles", roles)
                .audience("public")
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plus(ACCESS_TOKEN_TTL)))
                .build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .keyID(jwtKeyProperties.keyId())
                .type(JOSEObjectType.JWT)
                .build();
        SignedJWT token = new SignedJWT(header, claims);
        try {
            token.sign(new Ed25519Signer(signingKey));
            return token.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not sign access token", exception);
        }
    }
}
