package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.support.OrgIntegrationTestSupport;

@DisplayName("WebhookController Integration Tests")
class WebhookControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Test
    @DisplayName("GET webhooks returns 403 for foreign organization")
    void listWebhooks_shouldReturn403_whenForeignOrganization() throws Exception {
        Organization other = createOtherOrganization("WH-OTHER");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/webhooks", other.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET webhooks returns 401 when not authenticated")
    void listWebhooks_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/webhooks", organization.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
