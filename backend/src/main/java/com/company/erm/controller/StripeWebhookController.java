package com.company.erm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.service.StripeBillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Stripe Webhooks", description = "Stripe billing event receiver")
@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

    private final StripeBillingService stripeBillingService;

    public StripeWebhookController(StripeBillingService stripeBillingService) {
        this.stripeBillingService = stripeBillingService;
    }

    @Operation(summary = "Handle Stripe Webhook")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripeBillingService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
