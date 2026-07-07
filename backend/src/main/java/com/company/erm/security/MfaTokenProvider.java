package com.company.erm.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.company.erm.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Component
public class MfaTokenProvider {

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String MFA_PENDING = "mfa_pending";
    private static final long MFA_TOKEN_TTL_MS = 5 * 60 * 1000L;

    private final JwtProperties jwtProperties;

    public MfaTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateMfaPendingToken(UUID userId, UUID organizationId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + MFA_TOKEN_TTL_MS);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("organizationId", organizationId.toString())
                .claim(PURPOSE_CLAIM, MFA_PENDING)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public UUID getUserIdFromMfaToken(String token) {
        Claims claims = parseAndValidate(token);
        if (!MFA_PENDING.equals(claims.get(PURPOSE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Invalid MFA token purpose");
        }
        return UUID.fromString(claims.getSubject());
    }

    private Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
