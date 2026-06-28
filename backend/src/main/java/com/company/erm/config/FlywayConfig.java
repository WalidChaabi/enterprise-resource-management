package com.company.erm.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway configuration for database migrations.
 *
 * Demo seed scripts live under {@code db/demo-migration} (sibling of {@code db/migration},
 * not nested inside it) and run only when {@code erm.demo-data.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Autowired
    private DataSource dataSource;

    @Value("${spring.flyway.repair-on-migrate:false}")
    private boolean repairOnMigrate;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${erm.demo-data.enabled:false}")
    private boolean demoDataEnabled;

    @Bean
    public Flyway flyway() {
        boolean isDev = activeProfile.contains("dev");

        String[] locations = demoDataEnabled
                ? new String[]{"classpath:db/migration", "classpath:db/demo-migration"}
                : new String[]{"classpath:db/migration"};

        var config = Flyway.configure()
                .dataSource(dataSource)
                .schemas("erm")
                .locations(locations)
                .validateOnMigrate(true)
                .cleanOnValidationError(false);

        if (!demoDataEnabled) {
            config.ignoreMigrationPatterns("*:missing");
        }

        Flyway flyway = config.load();

        if (demoDataEnabled) {
            logger.info("Flyway demo migrations enabled (classpath:db/demo-migration)");
        } else {
            logger.info("Flyway demo migrations disabled");
        }

        if (isDev && repairOnMigrate) {
            logger.warn("Flyway repair-on-migrate is enabled. This should only be used in development.");
            try {
                flyway.repair();
                logger.info("Flyway checksums repaired successfully.");
            } catch (Exception e) {
                logger.error("Failed to repair Flyway checksums", e);
                throw new RuntimeException("Flyway repair failed", e);
            }
        } else if (!isDev) {
            logger.info("Flyway validation is strict in production.");
        }

        flyway.migrate();

        return flyway;
    }
}
