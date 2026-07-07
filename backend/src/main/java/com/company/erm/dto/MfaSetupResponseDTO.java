package com.company.erm.dto;

public record MfaSetupResponseDTO(
        String secret,
        String qrCodeUri,
        String issuer,
        String accountName
) {
}
