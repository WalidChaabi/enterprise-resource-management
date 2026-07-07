package com.company.erm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
@SecurityScheme(
        name = OpenApiConfig.BEARER_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT access token via Authorization header (tests and API clients)")
@SecurityScheme(
        name = OpenApiConfig.COOKIE_SCHEME,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "erm_access_token",
        description = "httpOnly access cookie set by POST /auth/login (browser SPA)")
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";
    public static final String COOKIE_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI ermOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERM Platform API")
                        .description("""
                                Enterprise resource management REST API.
                                Org-scoped resources live under `/api/v1/organizations/{organizationId}`.
                                Settings and auth endpoints are session-scoped and unversioned — see docs/API_VERSIONING.md.
                                """)
                        .version("v1")
                        .contact(new Contact().name("ERM Platform")))
                .components(new Components())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(COOKIE_SCHEME));
    }
}
