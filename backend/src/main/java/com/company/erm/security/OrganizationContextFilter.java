package com.company.erm.security;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Ensures the JWT active organization matches {@code /api/v1/organizations/{organizationId}} path segments.
 * Users must call {@code POST /api/organizations/switch} before accessing another organization.
 */
@Component
public class OrganizationContextFilter extends OncePerRequestFilter {

    static final Pattern ORGANIZATION_PATH_PATTERN =
            Pattern.compile("^/api/v1/organizations/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(?:/|$)");

    private final JwtTokenProvider tokenProvider;
    private final AuthCookieService authCookieService;

    public OrganizationContextFilter(JwtTokenProvider tokenProvider, AuthCookieService authCookieService) {
        this.tokenProvider = tokenProvider;
        this.authCookieService = authCookieService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID pathOrganizationId = extractOrganizationIdFromPath(request.getRequestURI());
        if (pathOrganizationId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token) || !tokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID tokenOrganizationId = tokenProvider.getOrganizationIdFromToken(token);
        if (tokenOrganizationId == null || !tokenOrganizationId.equals(pathOrganizationId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"code\":\"ORG_ACCESS_DENIED\",\"details\":[\"JWT organization does not match requested organization\"]}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    static UUID extractOrganizationIdFromPath(String requestUri) {
        if (requestUri == null) {
            return null;
        }
        Matcher matcher = ORGANIZATION_PATH_PATTERN.matcher(requestUri);
        if (!matcher.find()) {
            return null;
        }
        return UUID.fromString(matcher.group(1));
    }

    private String resolveToken(HttpServletRequest request) {
        String cookieToken = authCookieService.readAccessToken(request);
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
