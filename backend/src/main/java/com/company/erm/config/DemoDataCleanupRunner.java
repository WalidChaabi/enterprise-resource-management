package com.company.erm.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.repository.OrganizationRepository;

/**
 * Removes Flyway V3 demo organization when demo data is disabled (production).
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "erm.demo-data.enabled", havingValue = "false")
public class DemoDataCleanupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataCleanupRunner.class);
    private static final UUID DEMO_ORGANIZATION_ID =
        UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private final OrganizationRepository organizationRepository;

    public DemoDataCleanupRunner(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        organizationRepository.findById(DEMO_ORGANIZATION_ID).ifPresent(org -> {
            if ("DEMO".equals(org.getCode())) {
                organizationRepository.delete(org);
                logger.info("Removed demo organization '{}' (demo data disabled)", org.getCode());
            }
        });
    }
}
