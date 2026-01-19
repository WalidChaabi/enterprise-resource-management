package com.example.erm_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.company.erm.config.JwtProperties;
import com.company.erm.config.UiConfigProperties;
import com.company.erm.config.SettingsShortcutsProperties;

@SpringBootApplication(scanBasePackages = {"com.example.erm_backend", "com.company.erm"})
@EnableJpaRepositories(basePackages = "com.company.erm.repository")
@EnableConfigurationProperties({JwtProperties.class, UiConfigProperties.class, SettingsShortcutsProperties.class})
public class ErmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErmBackendApplication.class, args);
	}

}
