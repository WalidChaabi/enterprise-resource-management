package com.company.erm.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.entity.WebhookDelivery;
import com.company.erm.entity.WebhookEndpoint;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.WebhookDeliveryRepository;
import com.company.erm.repository.WebhookEndpointRepository;
import com.company.erm.webhook.WebhookDeliveryStatus;
import com.company.erm.webhook.WebhookEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebhookDispatchService {

    private final OrganizationRepository organizationRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final EntitlementService entitlementService;
    private final ObjectMapper objectMapper;

    public WebhookDispatchService(
            OrganizationRepository organizationRepository,
            WebhookEndpointRepository endpointRepository,
            WebhookDeliveryRepository deliveryRepository,
            EntitlementService entitlementService,
            ObjectMapper objectMapper) {
        this.organizationRepository = organizationRepository;
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.entitlementService = entitlementService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void dispatch(UUID organizationId, WebhookEventType eventType, Map<String, Object> data) {
        var organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null || !entitlementService.hasEntitlement(organization, com.company.erm.entitlement.PlanEntitlement.WEBHOOKS)) {
            return;
        }

        List<WebhookEndpoint> endpoints = endpointRepository.findByOrganizationIdAndActiveTrue(organizationId);
        if (endpoints.isEmpty()) {
            return;
        }

        String eventName = eventType.eventName();
        String payloadJson = buildPayload(organizationId, eventName, data);

        for (WebhookEndpoint endpoint : endpoints) {
            if (!endpoint.subscribesTo(eventName)) {
                continue;
            }
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setEndpointId(endpoint.getId());
            delivery.setOrganizationId(organizationId);
            delivery.setEventType(eventName);
            delivery.setPayloadJson(payloadJson);
            delivery.setStatus(WebhookDeliveryStatus.PENDING);
            delivery.setNextRetryAt(LocalDateTime.now());
            deliveryRepository.save(delivery);
        }
    }

    private String buildPayload(UUID organizationId, String eventName, Map<String, Object> data) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("id", UUID.randomUUID().toString());
        envelope.put("type", eventName);
        envelope.put("createdAt", LocalDateTime.now().toString());
        envelope.put("organizationId", organizationId.toString());
        envelope.put("data", data);
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize webhook payload", ex);
        }
    }
}
