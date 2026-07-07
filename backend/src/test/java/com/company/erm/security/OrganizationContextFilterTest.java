package com.company.erm.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrganizationContextFilter path parsing")
class OrganizationContextFilterTest {

    @Test
    @DisplayName("extractOrganizationIdFromPath parses versioned organization URLs")
    void extractOrganizationIdFromPath_shouldParseOrganizationSegment() {
        UUID organizationId = UUID.randomUUID();

        assertThat(OrganizationContextFilter.extractOrganizationIdFromPath(
                "/api/v1/organizations/" + organizationId + "/employees"))
                .isEqualTo(organizationId);

        assertThat(OrganizationContextFilter.extractOrganizationIdFromPath(
                "/api/v1/organizations/" + organizationId))
                .isEqualTo(organizationId);
    }

    @Test
    @DisplayName("extractOrganizationIdFromPath ignores non-organization URLs")
    void extractOrganizationIdFromPath_shouldReturnNullForOtherPaths() {
        assertThat(OrganizationContextFilter.extractOrganizationIdFromPath("/api/organizations/switch"))
                .isNull();
        assertThat(OrganizationContextFilter.extractOrganizationIdFromPath("/api/settings/profile"))
                .isNull();
        assertThat(OrganizationContextFilter.extractOrganizationIdFromPath("/auth/login"))
                .isNull();
    }
}
