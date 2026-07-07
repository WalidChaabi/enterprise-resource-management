package com.company.erm.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.SearchEntityType;
import com.company.erm.dto.SearchRequestDTO;
import com.company.erm.dto.SearchResultDTO;
import com.company.erm.service.ExportService;
import com.company.erm.service.SearchService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for global search and filtering.
 * 
 * Provides endpoints for:
 * - Global search across all entity types
 * - Advanced filtering with pagination and sorting
 * 
 * Permissions: All authenticated users (with entity-specific restrictions)
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Search", description = "Global search and CSV export")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/search")
public class SearchController {

    private final SearchService searchService;
    private final ExportService exportService;

    public SearchController(SearchService searchService, ExportService exportService) {
        this.searchService = searchService;
        this.exportService = exportService;
    }

    /**
     * Global search across multiple entity types.
     * 
     * Searches across projects, employees, tasks, departments, time entries, and leave requests.
     * 
     * @param organizationId Organization ID
     * @param query Search query string
     * @param entityTypes Optional comma-separated list of entity types to search (PROJECT, EMPLOYEE, TASK, DEPARTMENT, TIME_ENTRY, LEAVE_REQUEST)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sortBy Sort field: relevance, name, date (default: relevance)
     * @param sortDirection Sort direction: asc, desc (default: desc)
     * @param authentication Current authenticated user
     * @return Search results with pagination
     */
    @Operation(summary = "Search")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    @PreAuthorize("@organizationSecurity.hasOrgAccess(#organizationId)")
    public ResponseEntity<SearchResultDTO> search(
            @PathVariable UUID organizationId,
            @RequestParam String query,
            @RequestParam(required = false) String entityTypes,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        
        // Parse entity types
        List<SearchEntityType> types = null;
        if (entityTypes != null && !entityTypes.trim().isEmpty()) {
            types = Arrays.stream(entityTypes.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(SearchEntityType::valueOf)
                .collect(Collectors.toList());
        }
        
        SearchRequestDTO request = new SearchRequestDTO(
            query,
            types,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        SearchResultDTO results = searchService.search(organizationId, request, currentUserEmail);
        return ResponseEntity.ok(results);
    }

    /**
     * Export search results to CSV format.
     * 
     * @param organizationId Organization ID
     * @param query Search query string
     * @param entityTypes Optional comma-separated list of entity types to search
     * @param sortBy Sort field: relevance, name, date (default: relevance)
     * @param sortDirection Sort direction: asc, desc (default: desc)
     * @param authentication Current authenticated user
     * @return CSV file download
     */
    @Operation(summary = "Export To CSV")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/export/csv")
    @PreAuthorize("@organizationSecurity.hasOrgAccess(#organizationId)")
    public ResponseEntity<byte[]> exportToCSV(
            @PathVariable UUID organizationId,
            @RequestParam String query,
            @RequestParam(required = false) String entityTypes,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            Authentication authentication) throws IOException {
        
        String currentUserEmail = authentication.getName();
        
        // Parse entity types
        List<SearchEntityType> types = null;
        if (entityTypes != null && !entityTypes.trim().isEmpty()) {
            types = Arrays.stream(entityTypes.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(SearchEntityType::valueOf)
                .collect(Collectors.toList());
        }
        
        // Get all results (no pagination for export)
        SearchRequestDTO request = new SearchRequestDTO(
            query,
            types,
            0,
            10000, // Large page size to get all results
            sortBy,
            sortDirection
        );
        
        SearchResultDTO results = searchService.search(organizationId, request, currentUserEmail);
        byte[] csvContent = exportService.exportToCSV(results);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            "search_results_" + System.currentTimeMillis() + ".csv");
        headers.setContentLength(csvContent.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(csvContent);
    }

    /**
     * Export search results to PDF format.
     * 
     * @param organizationId Organization ID
     * @param query Search query string
     * @param entityTypes Optional comma-separated list of entity types to search
     * @param sortBy Sort field: relevance, name, date (default: relevance)
     * @param sortDirection Sort direction: asc, desc (default: desc)
     * @param authentication Current authenticated user
     * @return PDF file download
     */
    @Operation(summary = "Export To PDF")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/export/pdf")
    @PreAuthorize("@organizationSecurity.hasOrgAccess(#organizationId)")
    public ResponseEntity<byte[]> exportToPDF(
            @PathVariable UUID organizationId,
            @RequestParam String query,
            @RequestParam(required = false) String entityTypes,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            Authentication authentication) throws IOException {
        
        String currentUserEmail = authentication.getName();
        
        // Parse entity types
        List<SearchEntityType> types = null;
        if (entityTypes != null && !entityTypes.trim().isEmpty()) {
            types = Arrays.stream(entityTypes.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(SearchEntityType::valueOf)
                .collect(Collectors.toList());
        }
        
        // Get all results (no pagination for export)
        SearchRequestDTO request = new SearchRequestDTO(
            query,
            types,
            0,
            10000, // Large page size to get all results
            sortBy,
            sortDirection
        );
        
        SearchResultDTO results = searchService.search(organizationId, request, currentUserEmail);
        byte[] pdfContent = exportService.exportToPDF(results);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "search_results_" + System.currentTimeMillis() + ".pdf");
        headers.setContentLength(pdfContent.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfContent);
    }
}
