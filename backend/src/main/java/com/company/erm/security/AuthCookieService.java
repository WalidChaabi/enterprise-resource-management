package com.company.erm.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.company.erm.config.AuthCookieProperties;
import com.company.erm.config.JwtProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthCookieService {

    private final AuthCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public AuthCookieService(AuthCookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public void writeAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                cookieProperties.getAccessName(),
                accessToken,
                (int) (jwtProperties.getExpiration() / 1000),
                "/"
        ).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                cookieProperties.getRefreshName(),
                refreshToken,
                (int) (jwtProperties.getRefreshExpiration() / 1000),
                "/auth"
        ).toString());
    }

    public void writeAccessCookie(HttpServletResponse response, String accessToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                cookieProperties.getAccessName(),
                accessToken,
                (int) (jwtProperties.getExpiration() / 1000),
                "/"
        ).toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                cookieProperties.getAccessName(), "", 0, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                cookieProperties.getRefreshName(), "", 0, "/auth").toString());
    }

    public String readAccessToken(HttpServletRequest request) {
        return readCookie(request, cookieProperties.getAccessName());
    }

    public String readRefreshToken(HttpServletRequest request) {
        return readCookie(request, cookieProperties.getRefreshName());
    }

    private ResponseCookie buildCookie(String name, String value, int maxAgeSeconds, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
