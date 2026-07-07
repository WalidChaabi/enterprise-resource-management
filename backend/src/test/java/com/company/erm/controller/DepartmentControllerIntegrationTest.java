package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.support.OrgIntegrationTestSupport;
import com.company.erm.util.TestDataFactory;

@DisplayName("DepartmentController Integration Tests")
class DepartmentControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Test
    @DisplayName("GET departments returns 200 for organization member")
    void listDepartments_shouldReturn200_whenMember() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/departments", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET departments returns 403 for foreign organization")
    void listDepartments_shouldReturn403_whenForeignOrganization() throws Exception {
        Organization other = createOtherOrganization("DEPT-OTHER");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/departments", other.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET departments returns 403 when user lacks department role")
    void listDepartments_shouldReturn403_whenUserLacksRole() throws Exception {
        Role employeeRole = TestDataFactory.createRole(organization, "EMPLOYEE", "Employee");
        roleRepository.save(employeeRole);

        User employee = TestDataFactory.createUser(
                organization, department, "employee@example.com", "password", passwordEncoder);
        userRepository.save(employee);
        userRoleRepository.save(TestDataFactory.createUserRole(employee, employeeRole));

        String employeeToken = tokenForOrganization(employee, organization);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/departments", organization.getId())
                        .header("Authorization", "Bearer " + employeeToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
