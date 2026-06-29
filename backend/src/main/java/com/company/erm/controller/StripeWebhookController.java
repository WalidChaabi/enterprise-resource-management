package com.company.erm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.service.StripeBillingService;

@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

    private final StripeBillingService stripeBillingService;

    public StripeWebhookController(StripeBillingService stripeBillingService) {
        this.stripeBillingService = stripeBillingService;
    }

    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripeBillingService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
