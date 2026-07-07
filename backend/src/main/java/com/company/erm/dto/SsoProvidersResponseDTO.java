package com.company.erm.dto;

import java.util.List;

public record SsoProvidersResponseDTO(
        List<String> providers
) {
}
