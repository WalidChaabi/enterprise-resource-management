package com.company.erm.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway configuration for database migrations.
 * 
 * Migration Strategy:
 * - Never modify existing migration files after they've been applied to production
 * - If changes are needed, create a new migration file (V9__...)
 * - In development, checksum repair is allowed for flexibility
 * - In production, checksum validation is strict to prevent accidental changes
 * 
 * If you encounter checksum mismatch errors:
 * 1. In DEV: Set spring.flyway.repair-on-migrate=true (or run flyway repair manually)
 * 2. In PROD: Never repair - create a new migration instead
 */
@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Autowired
    private DataSource dataSource;

    @Value("${spring.flyway.repair-on-migrate:false}")
    private boolean repairOnMigrate;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public Flyway flyway() {
        boolean isDev = activeProfile.contains("dev");
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("erm")
                .locations("classpath:db/migration")
                .validateOnMigrate(true)
                .cleanOnValidationError(false)
                .load();

        // In development, allow repair if explicitly enabled
        if (isDev && repairOnMigrate) {
            logger.warn("⚠️  Flyway repair-on-migrate is enabled. This should only be used in development.");
            logger.warn("⚠️  Repairing checksums for mismatched migrations...");
            try {
                flyway.repair();
                logger.info("✅ Flyway checksums repaired successfully.");
            } catch (Exception e) {
                logger.error("❌ Failed to repair Flyway checksums", e);
                throw new RuntimeException("Flyway repair failed", e);
            }
            logger.warn("⚠️  If you see checksum mismatches, consider creating a new migration instead.");
        } else if (!isDev) {
            logger.info("✅ Flyway validation is strict in production. Checksum mismatches will fail.");
            logger.info("✅ If you need to change schema, create a new migration file instead of modifying existing ones.");
        }

        // Perform migration after potential repair
        flyway.migrate();
        
        return flyway;
    }
}

