package com.company.erm.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "erm.rate-limit")
@Validated
public class RateLimitProperties {

    @Min(1)
    private int requestsPerMinute = 10;

    @NotEmpty
    private List<String> paths = List.of(
            "/auth/login",
            "/auth/signup",
            "/auth/forgot-password",
            "/auth/mfa/verify",
            "/auth/refresh");

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
