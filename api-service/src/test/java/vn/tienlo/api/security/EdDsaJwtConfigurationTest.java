package vn.tienlo.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.tienlo.api.security.app.AccessTokenIssuer;

class EdDsaJwtConfigurationTest {
    @Test
    void API_AC_006_expiredSignedAccessTokenIsClassifiedAsTokenExpired() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair rawKeyPair = generator.generateKeyPair();
        JwtKeyProperties properties = new JwtKeyProperties(
                "test-kid",
                Base64.getEncoder().encodeToString(rawKeyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(rawKeyPair.getPublic().getEncoded()));
        EdDsaJwtConfiguration configuration = new EdDsaJwtConfiguration();
        OctetKeyPair signingKey = configuration.edDsaKeyPair(properties);
        Instant now = Instant.parse("2026-11-03T12:00:00Z");
        var decoder = configuration.jwtDecoder(
                signingKey,
                Clock.fixed(now, ZoneOffset.UTC),
                mock(StringRedisTemplate.class));
        SignedJWT expired = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.EdDSA).keyID("test-kid").type(JOSEObjectType.JWT).build(),
                new JWTClaimsSet.Builder()
                        .subject(UUID.randomUUID().toString())
                        .claim("sid", UUID.randomUUID().toString())
                        .claim("roles", List.of("PLAYER"))
                        .audience("public")
                        .issueTime(Date.from(now.minus(Duration.ofMinutes(16))))
                        .expirationTime(Date.from(now.minusSeconds(1)))
                        .build());
        expired.sign(new Ed25519Signer(signingKey));

        JwtValidationException exception =
                assertThrows(JwtValidationException.class, () -> decoder.decode(expired.serialize()));

        assertEquals("token_expired", exception.getErrors().iterator().next().getErrorCode());
    }

    @Test
    void API_R_030_issuesAndVerifiesEd25519PublicAudienceAccessTokens() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = generator.generateKeyPair();
        JwtKeyProperties properties = new JwtKeyProperties(
                "test-kid",
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        EdDsaJwtConfiguration configuration = new EdDsaJwtConfiguration();
        var signingKey = configuration.edDsaKeyPair(properties);
        var decoder = configuration.jwtDecoder(signingKey, Clock.systemUTC(), mock(StringRedisTemplate.class));

        Instant issuedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        AccessTokenIssuer issuer = new AccessTokenIssuer(
                signingKey,
                properties,
                Clock.fixed(issuedAt, ZoneOffset.UTC));
        UUID accountId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String serialized = issuer.issue(accountId, sessionId, List.of("PLAYER"));

        Jwt decoded = decoder.decode(serialized);
        assertEquals(accountId.toString(), decoded.getSubject());
        assertEquals(sessionId.toString(), decoded.getClaimAsString("sid"));
        assertEquals(List.of("PLAYER"), decoded.getClaimAsStringList("roles"));
        assertEquals(List.of("public"), decoded.getAudience());
        assertEquals("test-kid", decoded.getHeaders().get("kid"));
        assertEquals(issuedAt.plus(Duration.ofMinutes(15)), decoded.getExpiresAt());
    }

    @Test
    void API_R_030_rejectsMismatchedEd25519Keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair privatePair = generator.generateKeyPair();
        KeyPair publicPair = generator.generateKeyPair();
        JwtKeyProperties properties = new JwtKeyProperties(
                "test-kid",
                Base64.getEncoder().encodeToString(privatePair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(publicPair.getPublic().getEncoded()));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> new EdDsaJwtConfiguration().edDsaKeyPair(properties));
    }

    @Test
    void API_R_030_rejectsAValidlySignedTokenForAnotherAudience() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair rawKeyPair = generator.generateKeyPair();
        JwtKeyProperties properties = new JwtKeyProperties(
                "test-kid",
                Base64.getEncoder().encodeToString(rawKeyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(rawKeyPair.getPublic().getEncoded()));
        EdDsaJwtConfiguration configuration = new EdDsaJwtConfiguration();
        OctetKeyPair signingKey = configuration.edDsaKeyPair(properties);
        var decoder = configuration.jwtDecoder(signingKey, Clock.systemUTC(), mock(StringRedisTemplate.class));

        Instant issuedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        SignedJWT internalToken = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                        .keyID("test-kid")
                        .type(JOSEObjectType.JWT)
                        .build(),
                new JWTClaimsSet.Builder()
                        .subject(UUID.randomUUID().toString())
                        .claim("sid", UUID.randomUUID().toString())
                        .claim("roles", List.of("PLAYER"))
                        .audience("internal")
                        .issueTime(Date.from(issuedAt))
                        .expirationTime(Date.from(issuedAt.plus(Duration.ofMinutes(15))))
                        .build());
        internalToken.sign(new Ed25519Signer(signingKey));

        assertThrows(JwtValidationException.class, () -> decoder.decode(internalToken.serialize()));
    }
}
