package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.support.OrgIntegrationTestSupport;

import java.util.UUID;

@DisplayName("LeaveRequestController Integration Tests")
class LeaveRequestControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Test
    @DisplayName("GET leave requests returns paginated response for organization member")
    void listLeaveRequests_shouldReturnPaginatedPage_whenMember() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/leave-requests", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    @DisplayName("PUT leave request with empty body returns 400 validation error")
    void updateLeaveRequest_shouldReturn400_whenNoFieldsProvided() throws Exception {
        mockMvc.perform(put("/api/v1/organizations/{organizationId}/leave-requests/{id}",
                        organization.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET leave requests returns 403 for foreign organization")
    void listLeaveRequests_shouldReturn403_whenForeignOrganization() throws Exception {
        Organization other = createOtherOrganization("LR-OTHER");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/leave-requests", other.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
