package com.company.erm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.StripeProperties;
import com.company.erm.dto.settings.BillingPortalResponseDTO;
import com.company.erm.dto.settings.BillingSettingsResponseDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.StripeWebhookEvent;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.StripeWebhookEventRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionRetrieveParams;
import com.stripe.param.billingportal.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.Mode;

@Service
public class StripeBillingService {

    private static final Logger log = LoggerFactory.getLogger(StripeBillingService.class);

    private final StripeProperties stripeProperties;
    private final OrganizationRepository organizationRepository;
    private final StripeWebhookEventRepository webhookEventRepository;

    public StripeBillingService(
            StripeProperties stripeProperties,
            OrganizationRepository organizationRepository,
            StripeWebhookEventRepository webhookEventRepository) {
        this.stripeProperties = stripeProperties;
        this.organizationRepository = organizationRepository;
        this.webhookEventRepository = webhookEventRepository;
        if (stripeProperties.isEnabled()) {
            Stripe.apiKey = stripeProperties.getApiKey();
        }
    }

    public boolean isStripeEnabled() {
        return stripeProperties.isEnabled();
    }

    @Transactional(readOnly = true)
    public BillingSettingsResponseDTO getBillingSummary(Organization organization, long activeSeatCount) {
        BillingSettingsResponseDTO dto = new BillingSettingsResponseDTO();
        dto.setPlanName(formatPlanName(organization.getPlanCode()));
        dto.setCurrency(stripeProperties.getCurrency().toUpperCase(Locale.ROOT));
        dto.setSeatCount(activeSeatCount);

        String status = resolveStatus(organization);
        dto.setStatus(status);

        if (organization.getTrialEndsAt() != null && "TRIAL".equalsIgnoreCase(organization.getPlanCode())) {
            dto.setTrialEndsAt(organization.getTrialEndsAt().toLocalDate());
            if (organization.getTrialEndsAt().isAfter(LocalDateTime.now())) {
                dto.setNextBillingDate(organization.getTrialEndsAt().toLocalDate());
            }
        }

        if (stripeProperties.isEnabled() && organization.getStripeSubscriptionId() != null) {
            try {
                Subscription subscription = Subscription.retrieve(
                        organization.getStripeSubscriptionId(),
                        SubscriptionRetrieveParams.builder().addExpand("items.data.price").build(),
                        null);
                if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()
                        && subscription.getItems().getData().get(0).getPrice() != null) {
                    Long unitAmount = subscription.getItems().getData().get(0).getPrice().getUnitAmount();
                    if (unitAmount != null) {
                        dto.setAmount(BigDecimal.valueOf(unitAmount)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    }
                }
                if (subscription.getCurrentPeriodEnd() != null) {
                    dto.setNextBillingDate(LocalDate.ofInstant(
                            Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()),
                            ZoneOffset.UTC));
                }
            } catch (StripeException ex) {
                log.warn("Unable to load Stripe subscription for org {}: {}", organization.getId(), ex.getMessage());
            }
        }

        dto.setPortalAvailable(isStripeEnabled() && organization.getStripeCustomerId() != null);
        dto.setCheckoutAvailable(isStripeEnabled()
                && organization.getStripeCustomerId() != null
                && !"active".equalsIgnoreCase(status));
        return dto;
    }

    @Transactional
    public void ensureStripeCustomer(Organization organization, String adminEmail) {
        if (!stripeProperties.isEnabled() || organization.getStripeCustomerId() != null) {
            return;
        }
        try {
            Customer customer = Customer.create(CustomerCreateParams.builder()
                    .setEmail(adminEmail)
                    .setName(organization.getName())
                    .putMetadata("organization_id", organization.getId().toString())
                    .putMetadata("organization_code", organization.getCode())
                    .build());
            organization.setStripeCustomerId(customer.getId());
            organizationRepository.save(organization);
            log.info("Created Stripe customer {} for organization {}", customer.getId(), organization.getCode());
        } catch (StripeException ex) {
            log.error("Failed to create Stripe customer for org {}", organization.getId(), ex);
        }
    }

    @Transactional
    public BillingPortalResponseDTO createPortalSession(Organization organization) {
        requireStripeConfigured();
        if (organization.getStripeCustomerId() == null) {
            throw new BusinessException("BILLING_NO_CUSTOMER", "No billing account found for this organization",
                    HttpStatus.BAD_REQUEST);
        }
        try {
            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
                    SessionCreateParams.builder()
                            .setCustomer(organization.getStripeCustomerId())
                            .setReturnUrl(stripeProperties.getPortalReturnUrl())
                            .build());
            return new BillingPortalResponseDTO(session.getUrl());
        } catch (StripeException ex) {
            log.error("Stripe portal session failed for org {}", organization.getId(), ex);
            throw new BusinessException("BILLING_PORTAL_ERROR", "Unable to open billing portal", HttpStatus.BAD_GATEWAY);
        }
    }

    @Transactional
    public BillingPortalResponseDTO createCheckoutSession(Organization organization, long seatQuantity) {
        requireStripeConfigured();
        if (organization.getStripeCustomerId() == null) {
            throw new BusinessException("BILLING_NO_CUSTOMER", "No billing account found for this organization",
                    HttpStatus.BAD_REQUEST);
        }
        if (stripeProperties.getPriceId() == null || stripeProperties.getPriceId().isBlank()) {
            throw new BusinessException("BILLING_NOT_CONFIGURED", "Stripe price is not configured",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        long quantity = Math.max(1, seatQuantity);
        try {
            Session session = Session.create(
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(Mode.SUBSCRIPTION)
                            .setCustomer(organization.getStripeCustomerId())
                            .setSuccessUrl(stripeProperties.getCheckoutSuccessUrl())
                            .setCancelUrl(stripeProperties.getCheckoutCancelUrl())
                            .addLineItem(LineItem.builder()
                                    .setPrice(stripeProperties.getPriceId())
                                    .setQuantity(quantity)
                                    .build())
                            .putMetadata("organization_id", organization.getId().toString())
                            .build());
            return new BillingPortalResponseDTO(session.getUrl());
        } catch (StripeException ex) {
            log.error("Stripe checkout session failed for org {}", organization.getId(), ex);
            throw new BusinessException("BILLING_CHECKOUT_ERROR", "Unable to start checkout", HttpStatus.BAD_GATEWAY);
        }
    }

    @Transactional
    public void processWebhook(String payload, String signatureHeader) {
        requireStripeConfigured();
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            throw new BusinessException("WEBHOOK_NOT_CONFIGURED", "Stripe webhook secret is not configured",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new BusinessException("WEBHOOK_SIGNATURE_INVALID", "Invalid Stripe signature", HttpStatus.BAD_REQUEST);
        }

        if (webhookEventRepository.existsById(event.getId())) {
            log.debug("Skipping duplicate Stripe event {}", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.updated", "customer.subscription.deleted" -> handleSubscriptionEvent(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        StripeWebhookEvent record = new StripeWebhookEvent();
        record.setId(event.getId());
        record.setEventType(event.getType());
        webhookEventRepository.save(record);
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = event.getDataObjectDeserializer()
                .getObject()
                .filter(obj -> obj instanceof Session)
                .map(obj -> (Session) obj)
                .orElse(null);
        if (session == null) {
            return;
        }
        resolveOrganization(session.getMetadata(), session.getCustomer())
                .ifPresent(org -> {
                    if (session.getSubscription() != null) {
                        org.setStripeSubscriptionId(session.getSubscription());
                        org.setPlanCode("STARTER");
                        org.setSubscriptionStatus("active");
                        organizationRepository.save(org);
                    }
                });
    }

    private void handleSubscriptionEvent(Event event) {
        Subscription subscription = event.getDataObjectDeserializer()
                .getObject()
                .filter(obj -> obj instanceof Subscription)
                .map(obj -> (Subscription) obj)
                .orElse(null);
        if (subscription == null) {
            return;
        }
        resolveOrganization(subscription.getMetadata(), subscription.getCustomer())
                .ifPresent(org -> syncOrganizationFromSubscription(org, subscription));
    }

    private void handleInvoicePaid(Event event) {
        Invoice invoice = event.getDataObjectDeserializer()
                .getObject()
                .filter(obj -> obj instanceof Invoice)
                .map(obj -> (Invoice) obj)
                .orElse(null);
        if (invoice == null || invoice.getSubscription() == null) {
            return;
        }
        resolveOrganization(invoice.getMetadata(), invoice.getCustomer())
                .ifPresent(org -> {
                    org.setSubscriptionStatus("active");
                    organizationRepository.save(org);
                });
    }

    @Transactional
    public void syncOrganizationFromSubscription(Organization organization, Subscription subscription) {
        organization.setStripeSubscriptionId(subscription.getId());
        organization.setSubscriptionStatus(subscription.getStatus());
        if ("active".equals(subscription.getStatus()) || "trialing".equals(subscription.getStatus())) {
            organization.setPlanCode("STARTER");
        }
        if ("canceled".equals(subscription.getStatus()) || "unpaid".equals(subscription.getStatus())) {
            organization.setPlanCode("TRIAL");
        }
        organizationRepository.save(organization);
    }

    private Optional<Organization> resolveOrganization(Map<String, String> metadata, String customerId) {
        if (metadata != null && metadata.get("organization_id") != null) {
            try {
                return organizationRepository.findById(java.util.UUID.fromString(metadata.get("organization_id")));
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        if (customerId != null) {
            return organizationRepository.findByStripeCustomerId(customerId);
        }
        return Optional.empty();
    }

    private String resolveStatus(Organization organization) {
        if (organization.getSubscriptionStatus() != null && !organization.getSubscriptionStatus().isBlank()) {
            return organization.getSubscriptionStatus().toUpperCase(Locale.ROOT);
        }
        if ("TRIAL".equalsIgnoreCase(organization.getPlanCode())
                && organization.getTrialEndsAt() != null
                && organization.getTrialEndsAt().isAfter(LocalDateTime.now())) {
            return "TRIALING";
        }
        if (organization.getTrialEndsAt() != null && organization.getTrialEndsAt().isBefore(LocalDateTime.now())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }

    private String formatPlanName(String planCode) {
        if (planCode == null || planCode.isBlank()) {
            return "Free";
        }
        return switch (planCode.toUpperCase(Locale.ROOT)) {
            case "TRIAL" -> "Trial";
            case "STARTER" -> "Starter";
            case "PRO" -> "Pro";
            default -> planCode;
        };
    }

    private void requireStripeConfigured() {
        if (!stripeProperties.isEnabled()) {
            throw new BusinessException("BILLING_NOT_CONFIGURED",
                    "Stripe billing is not configured for this environment", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
