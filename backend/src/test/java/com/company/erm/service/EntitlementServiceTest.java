package com.company.erm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.erm.entitlement.PlanEntitlement;
import com.company.erm.entity.Organization;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceTest {

    @Mock
    private UserRepository userRepository;

    private EntitlementService entitlementService;

    @BeforeEach
    void setUp() {
        entitlementService = new EntitlementService(userRepository);
    }

    @Test
    void trialOrg_hasNoReportingEntitlement() {
        Organization org = trialOrg();

        assertThat(entitlementService.hasEntitlement(org, PlanEntitlement.REPORTING)).isFalse();
        assertThat(entitlementService.hasEntitlement(org, PlanEntitlement.WEBHOOKS)).isFalse();
    }

    @Test
    void starterOrg_hasReportingButNotWebhooks() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setPlanCode("STARTER");

        assertThat(entitlementService.hasEntitlement(org, PlanEntitlement.REPORTING)).isTrue();
        assertThat(entitlementService.hasEntitlement(org, PlanEntitlement.WEBHOOKS)).isFalse();
    }

    @Test
    void requireSeatAvailable_blocksWhenAtLimit() {
        Organization org = trialOrg();
        when(userRepository.countByOrganizationIdAndActiveTrue(org.getId())).thenReturn(5L);

        assertThatThrownBy(() -> entitlementService.requireSeatAvailable(org))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("SEAT_LIMIT_REACHED");
    }

    @Test
    void getEntitlements_includesSeatCounts() {
        Organization org = trialOrg();
        when(userRepository.countByOrganizationIdAndActiveTrue(org.getId())).thenReturn(2L);

        var dto = entitlementService.getEntitlements(org);

        assertThat(dto.getPlanCode()).isEqualTo("TRIAL");
        assertThat(dto.getSeatLimit()).isEqualTo(5);
        assertThat(dto.getSeatCount()).isEqualTo(2);
        assertThat(dto.isUpgradeRequired()).isTrue();
    }

    private Organization trialOrg() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setPlanCode("TRIAL");
        return org;
    }
}
