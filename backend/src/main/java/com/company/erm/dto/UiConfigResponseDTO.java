package com.company.erm.dto;

import lombok.Data;

@Data
public class UiConfigResponseDTO {
    private String documentationUrl;
    private String supportUrl;
    private boolean shortcutsEnabled;
}
