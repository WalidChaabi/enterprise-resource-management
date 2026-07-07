package com.company.erm.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@ConditionalOnProperty(name = "erm.oauth2.enabled", havingValue = "true")
public class OAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        List<ClientRegistration> registrations = new ArrayList<>();

        String googleClientId = environment.getProperty("GOOGLE_CLIENT_ID");
        String googleClientSecret = environment.getProperty("GOOGLE_CLIENT_SECRET");
        if (googleClientId != null && !googleClientId.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret != null ? googleClientSecret : "")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .clientName("Google")
                    .build());
        }

        String azureClientId = environment.getProperty("AZURE_CLIENT_ID");
        String azureClientSecret = environment.getProperty("AZURE_CLIENT_SECRET");
        String azureIssuer = environment.getProperty(
                "AZURE_ISSUER_URI",
                "https://login.microsoftonline.com/common/v2.0");
        if (azureClientId != null && !azureClientId.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("azure")
                    .clientId(azureClientId)
                    .clientSecret(azureClientSecret != null ? azureClientSecret : "")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .issuerUri(azureIssuer)
                    .clientName("Microsoft")
                    .build());
        }

        if (registrations.isEmpty()) {
            throw new IllegalStateException("erm.oauth2.enabled=true but no OAuth client IDs are configured");
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }
}
