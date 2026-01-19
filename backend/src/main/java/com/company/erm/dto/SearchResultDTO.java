package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private String query;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private Map<SearchEntityType, EntitySearchResults> resultsByType;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntitySearchResults {
        private SearchEntityType entityType;
        private long count;
        private List<SearchItemDTO> items;
    }
}
