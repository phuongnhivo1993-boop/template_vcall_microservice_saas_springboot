package com.vcall.notification.service;

import com.vcall.notification.entity.Notification;
import com.vcall.notification.entity.NotificationTemplate;
import com.vcall.notification.repository.NotificationTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final NotificationTemplateRepository templateRepository;

    public void sendEmail(Notification notification) {
        String subject = notification.getTitle();
        String body = notification.getBody();

        if (subject == null || subject.contains("{{")) {
            NotificationTemplate template = templateRepository.findByName(notification.getType().name()).orElse(null);
            if (template != null) {
                Map<String, String> variables = parseVariables(notification.getMetadata());
                subject = renderTemplate(template.getTitle() != null ? template.getTitle() : subject, variables);
                body = renderTemplate(template.getBody(), variables);
            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent to {} with subject {}", notification.getRecipientAddress(), subject);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public String renderTemplate(String template, Map<String, String> variables) {
        if (template == null) return null;
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private Map<String, String> parseVariables(String metadata) {
        return Map.of();
    }
}
