package com.campusfix.campusfixbackend.translation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "translation_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_text_hash", nullable = false, unique = true)
    private String originalTextHash;

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "target_language", nullable = false, length = 20)
    private String targetLanguage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
