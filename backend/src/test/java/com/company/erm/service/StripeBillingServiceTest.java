package com.company.erm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.erm.config.StripeProperties;
import com.company.erm.entity.Organization;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.StripeWebhookEventRepository;

@ExtendWith(MockitoExtension.class)
class StripeBillingServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private StripeWebhookEventRepository webhookEventRepository;

    private StripeProperties stripeProperties;
    private StripeBillingService stripeBillingService;

    @BeforeEach
    void setUp() {
        stripeProperties = new StripeProperties();
        stripeProperties.setEnabled(false);
        stripeBillingService = new StripeBillingService(
                stripeProperties, organizationRepository, webhookEventRepository);
    }

    @Test
    void getBillingSummary_returnsTrialStatusWhenStripeDisabled() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setPlanCode("TRIAL");
        org.setTrialEndsAt(LocalDateTime.now().plusDays(10));

        var summary = stripeBillingService.getBillingSummary(org, 3);

        assertThat(summary.getPlanName()).isEqualTo("Trial");
        assertThat(summary.getStatus()).isEqualTo("TRIALING");
        assertThat(summary.getSeatCount()).isEqualTo(3);
        assertThat(summary.isPortalAvailable()).isFalse();
        assertThat(summary.isCheckoutAvailable()).isFalse();
        assertThat(summary.getTrialEndsAt()).isEqualTo(org.getTrialEndsAt().toLocalDate());
    }

    @Test
    void isStripeEnabled_falseWhenApiKeyMissing() {
        stripeProperties.setEnabled(true);
        stripeProperties.setApiKey("");
        assertThat(stripeBillingService.isStripeEnabled()).isFalse();
    }
}
