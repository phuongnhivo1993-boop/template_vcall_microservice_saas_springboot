package com.vcall.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config-path:#{null}}")
    private String configPath;

    @Value("${firebase.database-url:#{null}}")
    private String databaseUrl;

    @Value("${firebase.project-id:#{null}}")
    private String projectId;

    @Value("${firebase.credentials-json:#{null}}")
    private String credentialsJson;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))) {
            log.info("Firebase app already initialized");
            return;
        }

        try {
            FirebaseOptions.Builder builder = FirebaseOptions.builder();

            if (credentialsJson != null && !credentialsJson.isBlank()) {
                try (InputStream stream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    builder.setCredentials(GoogleCredentials.fromStream(stream));
                }
                log.info("Firebase initialized from credentials JSON string");
            } else if (configPath != null && !configPath.isBlank()) {
                try (InputStream stream = new ClassPathResource(configPath).getInputStream()) {
                    builder.setCredentials(GoogleCredentials.fromStream(stream));
                }
                log.info("Firebase initialized from config path: {}", configPath);
            } else {
                try (InputStream stream = new ClassPathResource("firebase-service-account.json").getInputStream()) {
                    builder.setCredentials(GoogleCredentials.fromStream(stream));
                }
                log.info("Firebase initialized from default classpath: firebase-service-account.json");
            }

            if (databaseUrl != null && !databaseUrl.isBlank()) {
                builder.setDatabaseUrl(databaseUrl);
            }
            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId);
            }

            FirebaseApp.initializeApp(builder.build());
            log.info("Firebase FCM initialized successfully");
        } catch (IOException e) {
            log.warn("Firebase credentials not found. FCM push notifications will be disabled: {}", e.getMessage());
        }
    }
}
