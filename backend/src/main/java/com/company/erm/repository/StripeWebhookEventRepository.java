package com.company.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.StripeWebhookEvent;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, String> {
}
