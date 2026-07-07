package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.support.OrgIntegrationTestSupport;
import com.company.erm.util.TestDataFactory;

@DisplayName("SettingsController Integration Tests")
class SettingsControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Test
    @DisplayName("GET security settings returns 200 for authenticated user")
    void getSecuritySettings_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/settings/security")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST billing portal returns 403 for non-admin user")
    void createBillingPortal_shouldReturn403_whenNotOrgAdmin() throws Exception {
        Role employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleRepository.save(employeeRole);

        User employee = TestDataFactory.createUser(
                organization, department, "employee@example.com", "password", passwordEncoder);
        userRepository.save(employee);
        userRoleRepository.save(TestDataFactory.createUserRole(employee, employeeRole));

        String employeeToken = tokenForOrganization(employee, organization);

        mockMvc.perform(post("/api/settings/billing/portal")
                        .header("Authorization", "Bearer " + employeeToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET profile returns 401 when not authenticated")
    void getProfile_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/settings/profile")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
