package com.company.erm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.company.erm.entity.Organization;
import com.company.erm.entity.Project;
import com.company.erm.entity.ProjectStatus;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.support.OrgIntegrationTestSupport;

@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest extends OrgIntegrationTestSupport {

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;

    @BeforeEach
    void seedProject() {
        project = new Project();
        project.setOrganization(organizationRepository.getReferenceById(organization.getId()));
        project.setName("Integration Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.now());
        project.setBudget(BigDecimal.valueOf(10000));
        project.setManager(userRepository.getReferenceById(adminUser.getId()));
        project = projectRepository.save(project);
    }

    @Test
    @DisplayName("GET tasks returns 200 for organization member")
    void listTasks_shouldReturn200_whenMember() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/organizations/{organizationId}/projects/{projectId}/tasks",
                        organization.getId(),
                        project.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET tasks returns 403 for foreign organization")
    void listTasks_shouldReturn403_whenForeignOrganization() throws Exception {
        Organization other = createOtherOrganization("TASK-OTHER");

        mockMvc.perform(get(
                        "/api/v1/organizations/{organizationId}/projects/{projectId}/tasks",
                        other.getId(),
                        project.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
