package com.company.erm.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for JWT token generation and validation.
 * 
 * Security requirements:
 * - Secret must be at least 32 characters (256 bits) for HMAC-SHA256
 * - Expiration must be at least 1 hour (3600000 ms) for security
 * 
 * In production, JWT_SECRET must be set as an environment variable.
 * Never commit the actual secret to version control.
 */
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    /**
     * JWT signing secret key.
     * Must be at least 32 characters (256 bits) for HMAC-SHA256.
     * 
     * In production, this MUST be provided via JWT_SECRET environment variable.
     * Generate a secure secret using: openssl rand -base64 32
     */
    @NotBlank(message = "JWT secret is required. Set JWT_SECRET environment variable.")
    @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits) for security")
    private String secret;

    /**
     * JWT token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     * Minimum: 1 hour (3600000 ms) for security
     */
    @Min(value = 3600000, message = "JWT expiration must be at least 1 hour (3600000 ms) for security")
    private long expiration = 86400000; // 24 hours default

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
