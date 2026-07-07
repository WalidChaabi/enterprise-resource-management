package com.company.erm.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import com.company.erm.entity.Organization;
import com.company.erm.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DemoDataCleanupRunner Tests")
class DemoDataCleanupRunnerTest {

    private static final UUID DEMO_ORGANIZATION_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private DemoDataCleanupRunner demoDataCleanupRunner;

    @Test
    @DisplayName("removes demo organization when demo data is disabled")
    void run_shouldDeleteDemoOrganization() throws Exception {
        Organization demoOrg = new Organization();
        demoOrg.setId(DEMO_ORGANIZATION_ID);
        demoOrg.setCode("DEMO");
        demoOrg.setName("Demo Organization");

        when(organizationRepository.findById(DEMO_ORGANIZATION_ID)).thenReturn(Optional.of(demoOrg));

        demoDataCleanupRunner.run(applicationArguments);

        verify(organizationRepository).delete(demoOrg);
    }

    @Test
    @DisplayName("does nothing when demo organization is absent")
    void run_shouldSkipWhenDemoOrganizationMissing() throws Exception {
        when(organizationRepository.findById(DEMO_ORGANIZATION_ID)).thenReturn(Optional.empty());

        demoDataCleanupRunner.run(applicationArguments);

        verify(organizationRepository).findById(DEMO_ORGANIZATION_ID);
        assertThat(organizationRepository.findById(DEMO_ORGANIZATION_ID)).isEmpty();
    }
}
