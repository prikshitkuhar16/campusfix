package com.campusfix.campusfixbackend.translation.repository;

import com.campusfix.campusfixbackend.translation.entity.TranslationCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslationCacheRepository extends JpaRepository<TranslationCache, UUID> {

    Optional<TranslationCache> findByOriginalTextHashAndTargetLanguage(String originalTextHash, String targetLanguage);
}
