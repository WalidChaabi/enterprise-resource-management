package com.company.erm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.erm.entitlement.PlanEntitlement;
import com.company.erm.entity.Organization;
import com.company.erm.entity.WebhookDelivery;
import com.company.erm.entity.WebhookEndpoint;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.WebhookDeliveryRepository;
import com.company.erm.repository.WebhookEndpointRepository;
import com.company.erm.webhook.WebhookDeliveryStatus;
import com.company.erm.webhook.WebhookEventType;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WebhookDispatchServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private WebhookEndpointRepository endpointRepository;
    @Mock private WebhookDeliveryRepository deliveryRepository;
    @Mock private EntitlementService entitlementService;

    private WebhookDispatchService webhookDispatchService;
    private Organization organization;

    @BeforeEach
    void setUp() {
        webhookDispatchService = new WebhookDispatchService(
                organizationRepository,
                endpointRepository,
                deliveryRepository,
                entitlementService,
                new ObjectMapper());
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setPlanCode("PRO");
    }

    @Test
    void dispatch_skipsWhenEntitlementMissing() {
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(entitlementService.hasEntitlement(organization, PlanEntitlement.WEBHOOKS)).thenReturn(false);

        webhookDispatchService.dispatch(organization.getId(), WebhookEventType.LEAVE_APPROVED, java.util.Map.of("id", "1"));

        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void dispatch_enqueuesDeliveryForMatchingEndpoint() {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setId(UUID.randomUUID());
        endpoint.setOrganizationId(organization.getId());
        endpoint.setEventTypes(List.of("leave.approved"));
        endpoint.setActive(true);

        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(entitlementService.hasEntitlement(organization, PlanEntitlement.WEBHOOKS)).thenReturn(true);
        when(endpointRepository.findByOrganizationIdAndActiveTrue(organization.getId())).thenReturn(List.of(endpoint));

        webhookDispatchService.dispatch(
                organization.getId(),
                WebhookEventType.LEAVE_APPROVED,
                java.util.Map.of("leaveRequestId", "abc"));

        ArgumentCaptor<WebhookDelivery> captor = ArgumentCaptor.forClass(WebhookDelivery.class);
        verify(deliveryRepository).save(captor.capture());
        WebhookDelivery delivery = captor.getValue();
        assertThat(delivery.getEventType()).isEqualTo("leave.approved");
        assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.PENDING);
        assertThat(delivery.getPayloadJson()).contains("leave.approved");
    }
}
