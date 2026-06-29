package com.company.erm.controller;

import com.company.erm.dto.SignupRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.MockMvcTestConfiguration;
import com.company.erm.repository.OrganizationRepository;
import com.company.erm.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.company.erm.ErmBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(MockMvcTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Signup Integration Tests")
class SignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    @DisplayName("POST /auth/signup creates org, user, and session")
    void signup_createsOrganizationAndSession() throws Exception {
        SignupRequestDTO request = new SignupRequestDTO(
                "founder@acme.example",
                "securepass123",
                "Jane",
                "Founder",
                "Acme Consulting",
                true
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("founder@acme.example"))
                .andExpect(jsonPath("$.roles[0]").value("ORG_ADMIN"))
                .andExpect(cookie().exists("erm_access_token"))
                .andExpect(cookie().exists("erm_refresh_token"));

        assertThat(userRepository.existsByEmailIgnoreCase("founder@acme.example")).isTrue();
        assertThat(organizationRepository.findAll().stream()
                .anyMatch(org -> "Acme Consulting".equals(org.getName()))).isTrue();
    }

    @Test
    @DisplayName("POST /auth/signup rejects duplicate email")
    void signup_rejectsDuplicateEmail() throws Exception {
        SignupRequestDTO request = new SignupRequestDTO(
                "duplicate@acme.example",
                "securepass123",
                "Jane",
                "Founder",
                "Acme One",
                true
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequestDTO(
                                        "duplicate@acme.example",
                                        "otherpass456",
                                        "John",
                                        "Doe",
                                        "Other Org",
                                        true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_IN_USE"));
    }
}
