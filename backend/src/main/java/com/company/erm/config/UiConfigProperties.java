package com.company.erm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * UI configuration properties.
 */
@ConfigurationProperties(prefix = "ui")
@Validated
public class UiConfigProperties {

    @NotBlank(message = "UI documentationUrl is required")
    private String documentationUrl;

    @NotBlank(message = "UI supportUrl is required")
    private String supportUrl;

    private boolean shortcutsEnabled = true;

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public boolean isShortcutsEnabled() {
        return shortcutsEnabled;
    }

    public void setShortcutsEnabled(boolean shortcutsEnabled) {
        this.shortcutsEnabled = shortcutsEnabled;
    }
}
