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

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/projects")
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER')")
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDTO create(
        @PathVariable UUID orgId,
        @org.springframework.web.bind.annotation.RequestBody @Valid ProjectCreateDTO dto
    ) {
        return projectMapper.toDto(projectService.create(orgId, dto));
    }

    @GetMapping
    public List<ProjectResponseDTO> list(@PathVariable UUID orgId) {
        return projectService.listActive(orgId)
            .stream()
            .map(projectMapper::toDto)
            .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponseDTO getById(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId
    ) {
        return projectMapper.toDto(projectService.getById(orgId, projectId));
    }

    @RequestMapping(value = "/{projectId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ProjectResponseDTO update(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId,
        @org.springframework.web.bind.annotation.RequestBody @Valid ProjectUpdateDTO dto
    ) {
        return projectMapper.toDto(projectService.update(orgId, projectId, dto));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public void archive(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId
    ) {
        projectService.archive(orgId, projectId);
    }

    @DeleteMapping("/{projectId}/hard")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public void delete(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId
    ) {
        projectService.delete(orgId, projectId);
    }

    @PostMapping("/{projectId}/members/{employeeId}")
    public ProjectResponseDTO addMember(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId,
        @PathVariable UUID employeeId
    ) {
        return projectMapper.toDto(projectService.addMember(orgId, projectId, employeeId));
    }

    @DeleteMapping("/{projectId}/members/{employeeId}")
    public ProjectResponseDTO removeMember(
        @PathVariable UUID orgId,
        @PathVariable UUID projectId,
        @PathVariable UUID employeeId
    ) {
        return projectMapper.toDto(projectService.removeMember(orgId, projectId, employeeId));
    }
}
