package com.company.erm.webhook;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.company.erm.service.WebhookDeliveryProcessor;

@Component
public class WebhookDeliveryScheduler {

    private final WebhookDeliveryProcessor deliveryProcessor;

    public WebhookDeliveryScheduler(WebhookDeliveryProcessor deliveryProcessor) {
        this.deliveryProcessor = deliveryProcessor;
    }

    @Scheduled(fixedDelayString = "${erm.webhooks.poll-interval-ms:30000}")
    public void processDueDeliveries() {
        deliveryProcessor.processDueDeliveries(50);
    }
}
