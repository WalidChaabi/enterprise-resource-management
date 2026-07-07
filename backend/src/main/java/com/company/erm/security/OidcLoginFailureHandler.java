package com.company.erm.security;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.company.erm.config.OAuth2Properties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty(name = "erm.oauth2.enabled", havingValue = "true")
public class OidcLoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2Properties oauth2Properties;

    public OidcLoginFailureHandler(OAuth2Properties oauth2Properties) {
        this.oauth2Properties = oauth2Properties;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String frontend = oauth2Properties.getFrontendUrl().replaceAll("/$", "");
        response.sendRedirect(frontend + "/login?ssoError=1");
    }
}
