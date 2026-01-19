package com.company.erm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO {
    private String query; // Search term
    private List<SearchEntityType> entityTypes; // Which entity types to search (null = all)
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "relevance"; // relevance, name, date
    private String sortDirection = "desc"; // asc, desc
}
