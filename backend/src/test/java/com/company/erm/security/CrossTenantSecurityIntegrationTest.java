package com.company.erm.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.support.OrgIntegrationTestSupport;

@DisplayName("Cross-tenant security integration tests")
class CrossTenantSecurityIntegrationTest extends OrgIntegrationTestSupport {

    static Stream<Arguments> crossTenantEndpoints() {
        return Stream.of(
                Arguments.of("employees", "/employees"),
                Arguments.of("departments", "/departments"),
                Arguments.of("projects", "/projects"),
                Arguments.of("time-entries", "/time-entries"),
                Arguments.of("leave-requests", "/leave-requests"),
                Arguments.of("search", "/search?query=test"),
                Arguments.of("reporting-dashboard", "/reports/dashboard"),
                Arguments.of("reporting-capacity", "/reports/capacity?weekStart=2026-06-23"),
                Arguments.of("reporting-time", "/reports/time/summary"),
                Arguments.of("reporting-leave-balance", "/reports/leave/balance"),
                Arguments.of("webhooks", "/webhooks"),
                Arguments.of("roles", "/roles"));
    }

    @ParameterizedTest(name = "denies {0} for foreign organization")
    @MethodSource("crossTenantEndpoints")
    @DisplayName("GET cross-tenant endpoints return 403")
    void crossTenantGet_shouldReturn403(String label, String suffix) throws Exception {
        Organization otherOrganization = createOtherOrganization("CT-" + label.toUpperCase().replace('-', '_'));
        String path = "/api/v1/organizations/" + otherOrganization.getId() + suffix;

        mockMvc.perform(get(path)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    static Stream<Arguments> jwtMismatchEndpoints() {
        return Stream.of(
                Arguments.of("employees", "/employees"),
                Arguments.of("search", "/search?query=test"),
                Arguments.of("reporting-dashboard", "/reports/dashboard"));
    }

    @ParameterizedTest(name = "JWT mismatch on {0}")
    @MethodSource("jwtMismatchEndpoints")
    @DisplayName("JWT organization mismatch returns ORG_ACCESS_DENIED")
    void jwtMismatch_shouldReturnOrgAccessDenied(String label, String suffix) throws Exception {
        Organization otherOrganization = createOtherOrganization("JWT-" + label.toUpperCase());
        String path = "/api/v1/organizations/" + otherOrganization.getId() + suffix;

        mockMvc.perform(get(path)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ORG_ACCESS_DENIED"));
    }
}
