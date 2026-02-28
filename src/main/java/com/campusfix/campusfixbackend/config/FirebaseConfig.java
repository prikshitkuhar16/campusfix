package com.campusfix.campusfixbackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
