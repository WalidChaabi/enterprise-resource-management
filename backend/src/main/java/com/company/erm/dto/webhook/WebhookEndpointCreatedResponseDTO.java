package com.company.erm.dto.webhook;

public class WebhookEndpointCreatedResponseDTO extends WebhookEndpointResponseDTO {

    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
