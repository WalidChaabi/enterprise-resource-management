package com.company.erm.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.company.erm.config.OAuth2Properties;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.SsoAuthService;
import com.company.erm.service.SsoAuthService.SsoLoginResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty(name = "erm.oauth2.enabled", havingValue = "true")
public class OidcLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SsoAuthService ssoAuthService;
    private final AuthCookieService authCookieService;
    private final OAuth2Properties oauth2Properties;

    public OidcLoginSuccessHandler(
            SsoAuthService ssoAuthService,
            AuthCookieService authCookieService,
            OAuth2Properties oauth2Properties) {
        this.ssoAuthService = ssoAuthService;
        this.authCookieService = authCookieService;
        this.oauth2Properties = oauth2Properties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String registrationId = resolveRegistrationId(request);
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        Map<String, Object> claims = oidcUser.getClaims();
        String givenName = stringClaim(claims, "given_name");
        String familyName = stringClaim(claims, "family_name");

        SsoLoginResult result = ssoAuthService.completeOidcLogin(registrationId, subject, email, givenName, familyName);
        String frontend = oauth2Properties.getFrontendUrl().replaceAll("/$", "");

        if (result.mfaRequired()) {
            String redirect = frontend + "/login/mfa?token="
                    + URLEncoder.encode(result.mfaToken(), StandardCharsets.UTF_8);
            response.sendRedirect(redirect);
            return;
        }

        AuthSession session = result.session();
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        response.sendRedirect(frontend + "/dashboard");
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // /login/oauth2/code/{registrationId}
        int idx = uri.lastIndexOf('/');
        return idx >= 0 ? uri.substring(idx + 1) : "google";
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
