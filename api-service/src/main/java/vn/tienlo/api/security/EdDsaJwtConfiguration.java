package vn.tienlo.api.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.EdECPoint;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.ParseException;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;

/** API-R-030 and SEC-R-013: validate Ed25519 public-audience access tokens. */
@Configuration
@EnableConfigurationProperties(JwtKeyProperties.class)
public class EdDsaJwtConfiguration {
    @Bean
    OctetKeyPair edDsaKeyPair(JwtKeyProperties properties) {
        if (properties.keyId() == null
                || properties.keyId().isBlank()
                || properties.privateKey() == null
                || properties.privateKey().isBlank()
                || properties.publicKey() == null
                || properties.publicKey().isBlank()) {
            throw new IllegalStateException("TL_JWT_KEY_ID, TL_JWT_PRIVATE_KEY and TL_JWT_PUBLIC_KEY are required");
        }

        byte[] privateBytes = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            privateBytes = Base64.getDecoder().decode(properties.privateKey());
            byte[] publicBytes = Base64.getDecoder().decode(properties.publicKey());
            EdECPrivateKey privateKey = (EdECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            EdECPublicKey publicKey = (EdECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));

            verifyKeyPair(privateKey, publicKey);
            byte[] privateKeyBytes = privateKeyBytes(privateKey);
            try {
                return new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(publicKeyBytes(publicKey)))
                        .d(Base64URL.encode(privateKeyBytes))
                        .keyID(properties.keyId())
                        .algorithm(JWSAlgorithm.EdDSA)
                        .keyUse(KeyUse.SIGNATURE)
                        .build();
            } finally {
                Arrays.fill(privateKeyBytes, (byte) 0);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Configured Ed25519 JWT keys are invalid", exception);
        } finally {
            if (privateBytes != null) {
                Arrays.fill(privateBytes, (byte) 0);
            }
        }
    }

    @Bean
    JwtDecoder jwtDecoder(OctetKeyPair keyPair) {
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().size() == 1
                        && jwt.getAudience().contains("public")
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Required audience is missing", null));
        OAuth2TokenValidator<Jwt> requiredClaimsValidator = jwt -> {
            try {
                UUID.fromString(jwt.getSubject());
                UUID.fromString(jwt.getClaimAsString("sid"));
                List<String> roles = jwt.getClaimAsStringList("roles");
                Duration lifetime = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt());
                if (roles != null
                        && roles.stream().allMatch(role -> role != null && !role.isBlank())
                        && !lifetime.isNegative()
                        && lifetime.compareTo(Duration.ofMinutes(15)) <= 0) {
                    return OAuth2TokenValidatorResult.success();
                }
            } catch (IllegalArgumentException | NullPointerException exception) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Required JWT claims are invalid", null));
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Required JWT claims are missing or invalid", null));
        };
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), audienceValidator, requiredClaimsValidator);
        return token -> {
            try {
                SignedJWT signedJwt = SignedJWT.parse(token);
                JWSHeader header = signedJwt.getHeader();
                if (!JWSAlgorithm.EdDSA.equals(header.getAlgorithm())
                        || !keyPair.getKeyID().equals(header.getKeyID())
                        || !signedJwt.verify(new Ed25519Verifier(keyPair.toPublicJWK()))) {
                    throw new BadJwtException("JWT signature or key id is invalid");
                }

                JWTClaimsSet jwtClaims = signedJwt.getJWTClaimsSet();
                Map<String, Object> claims = new HashMap<>(jwtClaims.getClaims());
                Date issuedAt = jwtClaims.getIssueTime();
                Date expiresAt = jwtClaims.getExpirationTime();
                Date notBefore = jwtClaims.getNotBeforeTime();
                if (issuedAt != null) {
                    claims.put("iat", issuedAt.toInstant());
                }
                if (expiresAt != null) {
                    claims.put("exp", expiresAt.toInstant());
                }
                if (notBefore != null) {
                    claims.put("nbf", notBefore.toInstant());
                }

                Jwt jwt = new Jwt(
                        token,
                        issuedAt == null ? null : issuedAt.toInstant(),
                        expiresAt == null ? null : expiresAt.toInstant(),
                        header.toJSONObject(),
                        claims);
                var validation = validator.validate(jwt);
                if (validation.hasErrors()) {
                    throw new JwtValidationException("JWT claims are invalid", validation.getErrors());
                }
                return jwt;
            } catch (ParseException | JOSEException exception) {
                throw new BadJwtException("Could not parse or verify JWT", exception);
            }
        };
    }

    private static byte[] publicKeyBytes(EdECPublicKey publicKey) {
        EdECPoint point = publicKey.getPoint();
        byte[] y = point.getY().toByteArray();
        byte[] encoded = new byte[32];
        for (int index = 0; index < encoded.length; index++) {
            int sourceIndex = y.length - 1 - index;
            encoded[index] = sourceIndex >= 0 ? y[sourceIndex] : 0;
        }
        if (point.isXOdd()) {
            encoded[31] |= (byte) 0x80;
        }
        return encoded;
    }

    private static byte[] privateKeyBytes(EdECPrivateKey privateKey) {
        return privateKey.getBytes()
                .orElseThrow(() -> new IllegalStateException("Ed25519 private key bytes are unavailable"));
    }

    private static void verifyKeyPair(EdECPrivateKey privateKey, EdECPublicKey publicKey) throws Exception {
        byte[] challenge = "xproject-jwt-key-check".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        signer.update(challenge);
        byte[] signedChallenge = signer.sign();
        try {
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);
            verifier.update(challenge);
            if (!verifier.verify(signedChallenge)) {
                throw new IllegalArgumentException("The configured Ed25519 keys do not form a key pair");
            }
        } finally {
            Arrays.fill(signedChallenge, (byte) 0);
        }
    }
}
