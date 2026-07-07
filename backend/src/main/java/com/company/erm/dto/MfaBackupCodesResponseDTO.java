package com.company.erm.dto;

import java.util.List;

public record MfaBackupCodesResponseDTO(
        List<String> codes
) {
}
