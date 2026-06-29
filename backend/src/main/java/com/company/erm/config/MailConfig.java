package com.company.erm.config;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(prefix = "erm.mail", name = "enabled", havingValue = "true")
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        sender.setPort(mailProperties.getPort());
        if (!mailProperties.getUsername().isBlank()) {
            sender.setUsername(mailProperties.getUsername());
            sender.setPassword(mailProperties.getPassword());
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(mailProperties.isSmtpAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(mailProperties.isStartTls()));
        return sender;
    }
}
