package com.company.erm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.company.erm.config.AuthCookieProperties;
import com.company.erm.config.EncryptionProperties;
import com.company.erm.config.JwtProperties;
import com.company.erm.config.MailProperties;
import com.company.erm.config.RateLimitProperties;
import com.company.erm.config.SettingsShortcutsProperties;
import com.company.erm.config.StripeProperties;
import com.company.erm.config.UiConfigProperties;

@SpringBootApplication(scanBasePackages = "com.company.erm")
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.company.erm.repository")
@EnableConfigurationProperties({
    JwtProperties.class,
    AuthCookieProperties.class,
    EncryptionProperties.class,
    RateLimitProperties.class,
    UiConfigProperties.class,
    SettingsShortcutsProperties.class,
    StripeProperties.class,
    MailProperties.class
})
public class ErmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErmBackendApplication.class, args);
    }
}
