package com.company.erm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaEnrollConfirmDTO(
        @NotBlank String secret,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code
) {
}
