package com.company.erm.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.entity.WebhookDelivery;
import com.company.erm.entity.WebhookEndpoint;
import com.company.erm.repository.WebhookDeliveryRepository;
import com.company.erm.repository.WebhookEndpointRepository;
import com.company.erm.webhook.WebhookDeliveryStatus;
import com.company.erm.webhook.WebhookSignatureUtil;

@Service
public class WebhookDeliveryProcessor {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryProcessor.class);
    private static final int RESPONSE_BODY_LIMIT = 2000;
    private static final int[] RETRY_MINUTES = {1, 5, 15, 60, 240};

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final HttpClient httpClient;

    public WebhookDeliveryProcessor(
            WebhookDeliveryRepository deliveryRepository,
            WebhookEndpointRepository endpointRepository) {
        this.deliveryRepository = deliveryRepository;
        this.endpointRepository = endpointRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDelivery(UUID deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        if (delivery.getStatus() == WebhookDeliveryStatus.SUCCESS
                || delivery.getStatus() == WebhookDeliveryStatus.DEAD) {
            return;
        }
        if (delivery.getAttemptCount() >= delivery.getMaxAttempts()) {
            delivery.setStatus(WebhookDeliveryStatus.DEAD);
            deliveryRepository.save(delivery);
            return;
        }

        Optional<WebhookEndpoint> endpointOpt = endpointRepository.findById(delivery.getEndpointId());
        if (endpointOpt.isEmpty() || !endpointOpt.get().isActive()) {
            delivery.setStatus(WebhookDeliveryStatus.DEAD);
            delivery.setResponseBody("Endpoint missing or inactive");
            deliveryRepository.save(delivery);
            return;
        }

        WebhookEndpoint endpoint = endpointOpt.get();
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setLastAttemptAt(LocalDateTime.now());

        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = WebhookSignatureUtil.sign(endpoint.getSecret(), timestamp, delivery.getPayloadJson());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.getUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("X-ERM-Event", delivery.getEventType())
                    .header("X-ERM-Delivery-Id", delivery.getId().toString())
                    .header("X-ERM-Timestamp", timestamp)
                    .header("X-ERM-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayloadJson()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            delivery.setResponseStatus(response.statusCode());
            delivery.setResponseBody(truncate(response.body()));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.setStatus(WebhookDeliveryStatus.SUCCESS);
                delivery.setDeliveredAt(LocalDateTime.now());
            } else {
                markFailed(delivery);
            }
        } catch (Exception ex) {
            delivery.setResponseStatus(null);
            delivery.setResponseBody(truncate(ex.getMessage()));
            markFailed(delivery);
            log.warn("Webhook delivery {} failed: {}", delivery.getId(), ex.getMessage());
        }

        deliveryRepository.save(delivery);
    }

    @Transactional
    public int processDueDeliveries(int batchSize) {
        List<WebhookDelivery> due = deliveryRepository.findDueForDelivery(
                List.of(WebhookDeliveryStatus.PENDING, WebhookDeliveryStatus.FAILED),
                LocalDateTime.now(),
                Pageable.ofSize(batchSize));

        for (WebhookDelivery delivery : due) {
            processDelivery(delivery.getId());
        }
        return due.size();
    }

    private void markFailed(WebhookDelivery delivery) {
        if (delivery.getAttemptCount() >= delivery.getMaxAttempts()) {
            delivery.setStatus(WebhookDeliveryStatus.DEAD);
            return;
        }
        delivery.setStatus(WebhookDeliveryStatus.FAILED);
        delivery.setNextRetryAt(nextRetryAt(delivery.getAttemptCount()));
    }

    static LocalDateTime nextRetryAt(int attemptCount) {
        int index = Math.min(Math.max(attemptCount - 1, 0), RETRY_MINUTES.length - 1);
        return LocalDateTime.now().plusMinutes(RETRY_MINUTES[index]);
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= RESPONSE_BODY_LIMIT) {
            return value;
        }
        return value.substring(0, RESPONSE_BODY_LIMIT);
    }
}
