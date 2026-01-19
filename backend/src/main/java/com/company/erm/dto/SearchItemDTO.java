package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchItemDTO {
    private SearchEntityType entityType;
    private UUID id;
    private String title; // Name/title of the entity
    private String description; // Description or additional info
    private String highlight; // Highlighted matching text
    private LocalDate date; // Relevant date (start date, created date, etc.)
    private LocalDateTime createdAt;
    private Map<String, Object> metadata; // Additional entity-specific data
}
