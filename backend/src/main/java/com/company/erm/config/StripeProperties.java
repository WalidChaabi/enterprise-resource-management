package com.company.erm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private boolean enabled = false;
    private String apiKey = "";
    private String webhookSecret = "";
    /** Stripe Price ID for the default paid plan (per-seat monthly). */
    private String priceId = "";
    private String currency = "eur";
    private String portalReturnUrl = "http://localhost:4200/settings/billing";
    private String checkoutSuccessUrl = "http://localhost:4200/settings/billing?checkout=success";
    private String checkoutCancelUrl = "http://localhost:4200/settings/billing?checkout=cancel";

    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPortalReturnUrl() {
        return portalReturnUrl;
    }

    public void setPortalReturnUrl(String portalReturnUrl) {
        this.portalReturnUrl = portalReturnUrl;
    }

    public String getCheckoutSuccessUrl() {
        return checkoutSuccessUrl;
    }

    public void setCheckoutSuccessUrl(String checkoutSuccessUrl) {
        this.checkoutSuccessUrl = checkoutSuccessUrl;
    }

    public String getCheckoutCancelUrl() {
        return checkoutCancelUrl;
    }

    public void setCheckoutCancelUrl(String checkoutCancelUrl) {
        this.checkoutCancelUrl = checkoutCancelUrl;
    }
}
