package com.company.erm.dto;

public record LoginResponseDTO(
        boolean mfaRequired,
        String mfaToken,
        AuthResponse session
) {
    public static LoginResponseDTO complete(AuthResponse session) {
        return new LoginResponseDTO(false, null, session);
    }

    public static LoginResponseDTO mfaPending(String mfaToken) {
        return new LoginResponseDTO(true, mfaToken, null);
    }
}
