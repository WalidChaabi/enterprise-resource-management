package com.company.erm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import com.company.erm.security.OidcLoginFailureHandler;
import com.company.erm.security.OidcLoginSuccessHandler;

@Configuration
@ConditionalOnProperty(name = "erm.oauth2.enabled", havingValue = "true")
public class OAuth2SecurityCustomizer {

    private final OidcLoginSuccessHandler successHandler;
    private final OidcLoginFailureHandler failureHandler;

    public OAuth2SecurityCustomizer(
            OidcLoginSuccessHandler successHandler,
            OidcLoginFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public OAuth2LoginConfigurer oauth2LoginConfigurer() {
        return new OAuth2LoginConfigurer(successHandler, failureHandler);
    }

    public static class OAuth2LoginConfigurer {
        private final OidcLoginSuccessHandler successHandler;
        private final OidcLoginFailureHandler failureHandler;

        public OAuth2LoginConfigurer(
                OidcLoginSuccessHandler successHandler,
                OidcLoginFailureHandler failureHandler) {
            this.successHandler = successHandler;
            this.failureHandler = failureHandler;
        }

        public void apply(HttpSecurity http) throws Exception {
            http.oauth2Login(oauth -> oauth
                    .successHandler(successHandler)
                    .failureHandler(failureHandler));
        }

        public void permitOAuthEndpoints(
                AuthorizeHttpRequestsConfigurer<?>.AuthorizationManagerRequestMatcherRegistry auth) {
            auth.requestMatchers(
                    "/oauth2/**",
                    "/login/oauth2/**"
            ).permitAll();
        }
    }
}
