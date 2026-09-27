package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ExamSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSettingsRepository extends MongoRepository<ExamSettings, String> {

    ExamSettings findByDefaultSettings(Boolean isDefault);

    /**
     * Find settings by client ID
     */
    Optional<ExamSettings> findByClientId(String clientId);

    /**
     * Find active settings by client ID
     */
    Optional<ExamSettings> findByClientIdAndIsActiveTrue(String clientId);

    /**
     * Find default settings
     */
    Optional<ExamSettings> findByDefaultSettingsTrueAndIsActiveTrue();

    /**
     * Find all settings for a client
     */
    List<ExamSettings> findByClientIdOrderByCreatedAtDesc(String clientId);

    /**
     * Check if settings exist for a client
     */
    boolean existsByClientId(String clientId);

    /**
     * Find settings by client ID and active status
     */
    List<ExamSettings> findByClientIdAndIsActive(String clientId, Boolean isActive);

    /**
     * Find all active settings
     */
    List<ExamSettings> findByIsActiveTrue();

    /**
     * Find settings by retake policy
     */
    @Query("{ 'retakePolicy': { $regex: ?0, $options: 'i' } }")
    List<ExamSettings> findByRetakePolicyContainingIgnoreCase(String retakePolicy);

    /**
     * Find settings by passing score range
     */
    List<ExamSettings> findByPassingScoreBetween(Integer minScore, Integer maxScore);

    /**
     * Find settings by time limit range
     */
    List<ExamSettings> findByTimeLimitMinutesBetween(Integer minMinutes, Integer maxMinutes);
}
