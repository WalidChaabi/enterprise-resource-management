package com.company.erm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ConfigurationProperties(prefix = "erm.encryption")
@Validated
public class EncryptionProperties {

    @NotBlank
    @Size(min = 32, message = "erm.encryption.key must be at least 32 characters")
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
