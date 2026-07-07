package com.company.erm.dto.settings;

import java.util.List;

public class SecuritySettingsResponseDTO {
    private boolean ssoEnabled;
    private boolean requireMfa;
    private String ssoProvider;
    private boolean userMfaEnabled;
    private int unusedBackupCodes;
    private List<String> availableSsoProviders;

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean isRequireMfa() {
        return requireMfa;
    }

    public void setRequireMfa(boolean requireMfa) {
        this.requireMfa = requireMfa;
    }

    public String getSsoProvider() {
        return ssoProvider;
    }

    public void setSsoProvider(String ssoProvider) {
        this.ssoProvider = ssoProvider;
    }

    public boolean isUserMfaEnabled() {
        return userMfaEnabled;
    }

    public void setUserMfaEnabled(boolean userMfaEnabled) {
        this.userMfaEnabled = userMfaEnabled;
    }

    public int getUnusedBackupCodes() {
        return unusedBackupCodes;
    }

    public void setUnusedBackupCodes(int unusedBackupCodes) {
        this.unusedBackupCodes = unusedBackupCodes;
    }

    public List<String> getAvailableSsoProviders() {
        return availableSsoProviders;
    }

    public void setAvailableSsoProviders(List<String> availableSsoProviders) {
        this.availableSsoProviders = availableSsoProviders;
    }
}
