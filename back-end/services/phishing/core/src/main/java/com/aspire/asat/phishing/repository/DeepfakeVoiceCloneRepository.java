package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeepfakeVoiceCloneRepository extends MongoRepository<DeepfakeVoiceClone, String> {

    Optional<DeepfakeVoiceClone> findByVoiceCloneId(UUID voiceCloneId);

    Optional<DeepfakeVoiceClone> findByVoiceCloneIdAndClientId(UUID voiceCloneId, String clientId);

    @Query(value = "{ 'provider': ?0, 'status': ?1, 'externalVoiceId': { $ne: null }, "
            + "'isDeleted': { $ne: true } }", sort = "{ 'createdAt': -1 }")
    Optional<DeepfakeVoiceClone>
            findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(
                    VoiceCloneProvider provider, DeepfakeJobStatus status);

    /**
     * Counts other non-deleted clones that still reference the same provider voice id.
     * Treats missing {@code isDeleted} as not deleted (legacy documents).
     */
    @Query(value = "{ 'externalVoiceId': ?0, 'provider': ?1, 'voiceCloneId': { $ne: ?2 }, 'isDeleted': { $ne: true } }",
            count = true)
    long countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
            String externalVoiceId, VoiceCloneProvider provider, UUID voiceCloneId);
}
