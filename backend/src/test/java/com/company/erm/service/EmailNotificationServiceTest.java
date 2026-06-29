package com.company.erm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import com.company.erm.config.MailProperties;
import com.company.erm.entity.User;
import com.company.erm.entity.UserNotificationPreferences;
import com.company.erm.repository.UserNotificationPreferencesRepository;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserNotificationPreferencesRepository notificationPreferencesRepository;

    @Mock
    private MimeMessage mimeMessage;

    private MailProperties mailProperties;
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        mailProperties = new MailProperties();
        mailProperties.setEnabled(true);
        mailProperties.setFrom("noreply@test.com");
        mailProperties.setFromName("ERM");
        mailProperties.setFrontendUrl("http://localhost:4200");
        emailNotificationService = new EmailNotificationService(
                mailProperties, notificationPreferencesRepository, mailSender);
    }

    @Test
    void sendWorkflowNotification_skipsWhenMailDisabled() {
        mailProperties.setEnabled(false);
        emailNotificationService = new EmailNotificationService(
                mailProperties, notificationPreferencesRepository, null);

        emailNotificationService.sendWorkflowNotification(userWithEmail("user@test.com"), "Title", "Body");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendWorkflowNotification_skipsWhenUserDisabledEmail() {
        User user = userWithEmail("user@test.com");
        UserNotificationPreferences preferences = new UserNotificationPreferences();
        preferences.setEmailEnabled(false);
        when(notificationPreferencesRepository.findByUserId(user.getId())).thenReturn(Optional.of(preferences));

        emailNotificationService.sendWorkflowNotification(user, "Title", "Body");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendWorkflowNotification_sendsWhenEnabled() throws Exception {
        User user = userWithEmail("user@test.com");
        when(notificationPreferencesRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailNotificationService.sendWorkflowNotification(user, "Leave approved", "Your leave was approved");

        verify(mailSender).send(mimeMessage);
    }

    private static User userWithEmail(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName("Alex");
        return user;
    }
}
