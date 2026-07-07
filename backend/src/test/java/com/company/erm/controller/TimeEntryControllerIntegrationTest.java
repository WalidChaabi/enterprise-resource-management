package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.support.OrgIntegrationTestSupport;

@DisplayName("TimeEntryController Integration Tests")
class TimeEntryControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Test
    @DisplayName("GET time entries returns paginated response for organization member")
    void listTimeEntries_shouldReturnPaginatedPage_whenMember() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/time-entries", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    @DisplayName("GET time entries returns 403 for foreign organization")
    void listTimeEntries_shouldReturn403_whenForeignOrganization() throws Exception {
        Organization other = createOtherOrganization("TE-OTHER");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/time-entries", other.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
