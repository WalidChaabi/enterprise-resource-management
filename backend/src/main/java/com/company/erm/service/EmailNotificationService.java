package com.company.erm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.company.erm.config.MailProperties;
import com.company.erm.entity.User;
import com.company.erm.entity.UserNotificationPreferences;
import com.company.erm.repository.UserNotificationPreferencesRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final MailProperties mailProperties;
    private final UserNotificationPreferencesRepository notificationPreferencesRepository;
    private final JavaMailSender mailSender;

    public EmailNotificationService(
            MailProperties mailProperties,
            UserNotificationPreferencesRepository notificationPreferencesRepository,
            @Autowired(required = false) JavaMailSender mailSender) {
        this.mailProperties = mailProperties;
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.mailSender = mailSender;
    }

    public void sendWorkflowNotification(User user, String title, String description) {
        if (!mailProperties.isEnabled() || mailSender == null || user == null) {
            return;
        }
        if (!isEmailEnabled(user)) {
            return;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setSubject(title);
            helper.setText(buildHtmlBody(title, description, user.getFirstName()), true);
            mailSender.send(message);
            log.info("Workflow email sent to {}", user.getEmail());
        } catch (Exception ex) {
            log.warn("Failed to send workflow email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    private boolean isEmailEnabled(User user) {
        return notificationPreferencesRepository.findByUserId(user.getId())
                .map(UserNotificationPreferences::isEmailEnabled)
                .orElse(true);
    }

    private String buildHtmlBody(String title, String description, String firstName) {
        String greeting = firstName != null && !firstName.isBlank() ? "Hi " + escapeHtml(firstName) + "," : "Hi,";
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: system-ui, sans-serif; color: #111827; line-height: 1.5;">
                  <p>%s</p>
                  <h2 style="font-size: 1.125rem; margin: 0 0 0.5rem;">%s</h2>
                  <p style="margin: 0 0 1rem;">%s</p>
                  <p><a href="%s" style="color: #2563eb;">Open ERM</a></p>
                </body>
                </html>
                """.formatted(
                greeting,
                escapeHtml(title),
                escapeHtml(description),
                escapeHtml(mailProperties.getFrontendUrl()));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
