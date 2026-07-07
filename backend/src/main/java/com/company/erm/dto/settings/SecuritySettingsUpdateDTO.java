package com.company.erm.dto.settings;

public class SecuritySettingsUpdateDTO {
    private Boolean ssoEnabled;
    private Boolean requireMfa;
    private String ssoProvider;

    public Boolean getSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(Boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public Boolean getRequireMfa() {
        return requireMfa;
    }

    public void setRequireMfa(Boolean requireMfa) {
        this.requireMfa = requireMfa;
    }

    public String getSsoProvider() {
        return ssoProvider;
    }

    public void setSsoProvider(String ssoProvider) {
        this.ssoProvider = ssoProvider;
    }
}
