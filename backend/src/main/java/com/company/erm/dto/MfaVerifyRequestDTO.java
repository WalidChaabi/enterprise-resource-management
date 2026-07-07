package com.company.erm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaVerifyRequestDTO(
        @NotBlank String mfaToken,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$|^[A-Z0-9]{4}-[A-Z0-9]{4}$") String code
) {
}
