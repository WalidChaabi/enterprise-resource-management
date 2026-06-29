package com.company.erm.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.webhook.WebhookDeliveryResponseDTO;
import com.company.erm.dto.webhook.WebhookEndpointCreatedResponseDTO;
import com.company.erm.dto.webhook.WebhookEndpointRequestDTO;
import com.company.erm.dto.webhook.WebhookEndpointResponseDTO;
import com.company.erm.entitlement.PlanEntitlement;
import com.company.erm.entity.Organization;
import com.company.erm.entity.WebhookDelivery;
import com.company.erm.entity.WebhookEndpoint;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.WebhookDeliveryRepository;
import com.company.erm.repository.WebhookEndpointRepository;
import com.company.erm.security.PermissionChecker;
import com.company.erm.security.RoleCode;
import com.company.erm.webhook.WebhookEventType;

@Service
@Transactional
public class WebhookEndpointService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PermissionChecker permissionChecker;
    private final EntitlementService entitlementService;

    public WebhookEndpointService(
            WebhookEndpointRepository endpointRepository,
            WebhookDeliveryRepository deliveryRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            PermissionChecker permissionChecker,
            EntitlementService entitlementService) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.permissionChecker = permissionChecker;
        this.entitlementService = entitlementService;
    }

    @Transactional(readOnly = true)
    public List<WebhookEndpointResponseDTO> listEndpoints(UUID organizationId, String currentUserEmail) {
        requireAccess(organizationId, currentUserEmail);
        return endpointRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public WebhookEndpointCreatedResponseDTO createEndpoint(
            UUID organizationId,
            WebhookEndpointRequestDTO request,
            String currentUserEmail) {
        Organization organization = requireAccess(organizationId, currentUserEmail);
        validateEventTypes(request.getEventTypes());
        validateUrl(request.getUrl());

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setOrganizationId(organization.getId());
        endpoint.setUrl(request.getUrl().trim());
        endpoint.setDescription(request.getDescription());
        endpoint.setEventTypes(request.getEventTypes());
        endpoint.setActive(request.isActive());
        endpoint.setSecret(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));

        WebhookEndpoint saved = endpointRepository.save(endpoint);
        WebhookEndpointCreatedResponseDTO dto = new WebhookEndpointCreatedResponseDTO();
        copyToDto(saved, dto);
        dto.setSecret(saved.getSecret());
        return dto;
    }

    public WebhookEndpointResponseDTO updateEndpoint(
            UUID organizationId,
            UUID endpointId,
            WebhookEndpointRequestDTO request,
            String currentUserEmail) {
        requireAccess(organizationId, currentUserEmail);
        validateEventTypes(request.getEventTypes());
        validateUrl(request.getUrl());

        WebhookEndpoint endpoint = endpointRepository.findByIdAndOrganizationId(endpointId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "WEBHOOK_NOT_FOUND", "Webhook endpoint not found", HttpStatus.NOT_FOUND));

        endpoint.setUrl(request.getUrl().trim());
        endpoint.setDescription(request.getDescription());
        endpoint.setEventTypes(request.getEventTypes());
        endpoint.setActive(request.isActive());
        return toDto(endpointRepository.save(endpoint));
    }

    public void deleteEndpoint(UUID organizationId, UUID endpointId, String currentUserEmail) {
        requireAccess(organizationId, currentUserEmail);
        WebhookEndpoint endpoint = endpointRepository.findByIdAndOrganizationId(endpointId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "WEBHOOK_NOT_FOUND", "Webhook endpoint not found", HttpStatus.NOT_FOUND));
        endpointRepository.delete(endpoint);
    }

    @Transactional(readOnly = true)
    public Page<WebhookDeliveryResponseDTO> listDeliveries(
            UUID organizationId,
            UUID endpointId,
            String currentUserEmail,
            Pageable pageable) {
        requireAccess(organizationId, currentUserEmail);

        Page<WebhookDelivery> page;
        if (endpointId != null) {
            endpointRepository.findByIdAndOrganizationId(endpointId, organizationId)
                    .orElseThrow(() -> new BusinessException(
                            "WEBHOOK_NOT_FOUND", "Webhook endpoint not found", HttpStatus.NOT_FOUND));
            page = deliveryRepository.findByEndpointIdOrderByCreatedAtDesc(endpointId, pageable);
        } else {
            page = deliveryRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        }

        return page.map(delivery -> {
            WebhookDeliveryResponseDTO dto = toDeliveryDto(delivery);
            endpointRepository.findById(delivery.getEndpointId())
                    .ifPresent(endpoint -> dto.setEndpointUrl(endpoint.getUrl()));
            return dto;
        });
    }

    private Organization requireAccess(UUID organizationId, String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail, RoleCode.ORG_ADMIN);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(
                        "ORG_NOT_FOUND", "Organization not found", HttpStatus.NOT_FOUND));
        userRepository.findByEmail(currentUserEmail).ifPresent(user -> {
            if (!user.getOrganization().getId().equals(organizationId)) {
                throw new BusinessException("ORG_MISMATCH", "Organization mismatch", HttpStatus.FORBIDDEN);
            }
        });
        entitlementService.requireEntitlement(organization, PlanEntitlement.WEBHOOKS);
        return organization;
    }

    private void validateEventTypes(List<String> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            throw new BusinessException("WEBHOOK_EVENTS_REQUIRED", "Select at least one event type");
        }
        for (String eventType : eventTypes) {
            try {
                WebhookEventType.fromEventName(eventType);
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("WEBHOOK_EVENT_INVALID", "Unknown event type: " + eventType);
            }
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException("WEBHOOK_URL_REQUIRED", "Webhook URL is required");
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw new BusinessException("WEBHOOK_URL_INVALID", "Webhook URL must use HTTP or HTTPS");
        }
    }

    private WebhookEndpointResponseDTO toDto(WebhookEndpoint endpoint) {
        WebhookEndpointResponseDTO dto = new WebhookEndpointResponseDTO();
        copyToDto(endpoint, dto);
        return dto;
    }

    private void copyToDto(WebhookEndpoint endpoint, WebhookEndpointResponseDTO dto) {
        dto.setId(endpoint.getId());
        dto.setUrl(endpoint.getUrl());
        dto.setDescription(endpoint.getDescription());
        dto.setEventTypes(endpoint.getEventTypes());
        dto.setActive(endpoint.isActive());
        dto.setCreatedAt(endpoint.getCreatedAt());
        dto.setUpdatedAt(endpoint.getUpdatedAt());
    }

    private WebhookDeliveryResponseDTO toDeliveryDto(WebhookDelivery delivery) {
        WebhookDeliveryResponseDTO dto = new WebhookDeliveryResponseDTO();
        dto.setId(delivery.getId());
        dto.setEndpointId(delivery.getEndpointId());
        dto.setEventType(delivery.getEventType());
        dto.setStatus(delivery.getStatus());
        dto.setAttemptCount(delivery.getAttemptCount());
        dto.setMaxAttempts(delivery.getMaxAttempts());
        dto.setNextRetryAt(delivery.getNextRetryAt());
        dto.setLastAttemptAt(delivery.getLastAttemptAt());
        dto.setResponseStatus(delivery.getResponseStatus());
        dto.setResponseBody(delivery.getResponseBody());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setDeliveredAt(delivery.getDeliveredAt());
        return dto;
    }
}
