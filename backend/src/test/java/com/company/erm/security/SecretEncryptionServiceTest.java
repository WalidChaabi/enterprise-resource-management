package com.company.erm.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.company.erm.config.EncryptionProperties;

@DisplayName("SecretEncryptionService Tests")
class SecretEncryptionServiceTest {

    @Test
    @DisplayName("encrypt and decrypt round-trip preserves plaintext")
    void encryptDecrypt_shouldRoundTrip() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKey("test-encryption-key-minimum-32-characters-long");
        SecretEncryptionService service = new SecretEncryptionService(properties);

        String plaintext = "JBSWY3DPEHPK3PXP";
        String encrypted = service.encrypt(plaintext);

        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(service.decrypt(encrypted)).isEqualTo(plaintext);
    }
}
