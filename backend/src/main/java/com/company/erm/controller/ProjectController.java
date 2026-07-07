package com.company.erm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.ProjectCreateDTO;
import com.company.erm.dto.ProjectResponseDTO;
import com.company.erm.dto.ProjectUpdateDTO;
import com.company.erm.mapper.ProjectMapper;
import com.company.erm.service.ProjectService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Projects", description = "Project CRUD and membership")
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects")
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'PROJECT_MANAGER')")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(
        ProjectService projectService,
        ProjectMapper projectMapper
    ) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @Operation(summary = "Create")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDTO create(
        @PathVariable UUID organizationId,
        @org.springframework.web.bind.annotation.RequestBody @Valid ProjectCreateDTO dto
    ) {
        return projectMapper.toDto(projectService.create(organizationId, dto));
    }

    @Operation(summary = "List")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public List<ProjectResponseDTO> list(@PathVariable UUID organizationId) {
        return projectService.listActive(organizationId)
            .stream()
            .map(projectMapper::toDto)
            .toList();
    }

    @Operation(summary = "Get By Id")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{projectId}")
    public ProjectResponseDTO getById(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId
    ) {
        return projectMapper.toDto(projectService.getById(organizationId, projectId));
    }

    @RequestMapping(value = "/{projectId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ProjectResponseDTO update(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId,
        @org.springframework.web.bind.annotation.RequestBody @Valid ProjectUpdateDTO dto
    ) {
        return projectMapper.toDto(projectService.update(organizationId, projectId, dto));
    }

    @Operation(summary = "Archive")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
    public void archive(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId
    ) {
        projectService.archive(organizationId, projectId);
    }

    @Operation(summary = "Delete")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{projectId}/hard")
    @PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
    public void delete(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId
    ) {
        projectService.delete(organizationId, projectId);
    }

    @Operation(summary = "Add Member")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/{projectId}/members/{employeeId}")
    public ProjectResponseDTO addMember(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId,
        @PathVariable UUID employeeId
    ) {
        return projectMapper.toDto(projectService.addMember(organizationId, projectId, employeeId));
    }

    @Operation(summary = "Remove Member")
    @ApiResponse(responseCode = "200", description = "Success")
    @DeleteMapping("/{projectId}/members/{employeeId}")
    public ProjectResponseDTO removeMember(
        @PathVariable UUID organizationId,
        @PathVariable UUID projectId,
        @PathVariable UUID employeeId
    ) {
        return projectMapper.toDto(projectService.removeMember(organizationId, projectId, employeeId));
    }
}
