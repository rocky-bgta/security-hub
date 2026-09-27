package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.TranscriptionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranscriptionRepository extends MongoRepository<TranscriptionEntity, String> {
    Optional<TranscriptionEntity> findByAudioId(UUID audioId);
}
