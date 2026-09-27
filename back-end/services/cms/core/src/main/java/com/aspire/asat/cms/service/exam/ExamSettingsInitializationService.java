package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.model.ExamSettings;
import com.aspire.asat.cms.repository.ExamSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service to initialize default client exam settings on application startup
 */
@Slf4j
@Service
public class ExamSettingsInitializationService implements CommandLineRunner {

    private final ExamSettingsRepository examSettingsRepository;

    @Value("${default.exam.settings.duration-minutes:30}")
    private Integer defaultTimeLimitMinutes;

    @Value("${default.exam.settings.passing-score:70}")
    private Integer defaultPassingScore;

    @Value("${default.exam.settings.retake-policy:allowed}")
    private String defaultRetakePolicy;

    @Value("${default.exam.settings.total-questions:5}")
    private Integer totalQuestionCount;

    @Value("${default.exam.settings.distribution-strategy:EQUAL}")
    private String defaultDistributionStrategy;


    @Autowired
    public ExamSettingsInitializationService(ExamSettingsRepository examSettingsRepository) {
        this.examSettingsRepository = examSettingsRepository;
    }

    @Override
    public void run(String... args) {
        initializeDefaultSettings();
    }

    /**
     * Initialize default exam settings if they don't exist
     */
    private void initializeDefaultSettings() {
        try {
            // Check if default settings already exist
            if (examSettingsRepository.findByDefaultSettingsTrueAndIsActiveTrue().isPresent()) {
                log.info("Default client exam settings already exist, skipping initialization");
                return;
            }

            final var now = Instant.now();

            // Create default settings
            ExamSettings defaultSettings = ExamSettings.builder()
                    .id(UUID.randomUUID().toString())
                    .clientId(null) // Default settings don't belong to a specific client
                    .timeLimitMinutes(defaultTimeLimitMinutes) // Default 60 minutes
                    .passingScore(defaultPassingScore) // Default 70% passing score
                    .retakePolicy(defaultRetakePolicy) // Default retake policy
                    .totalQuestions(totalQuestionCount)
                    .distributionStrategy(defaultDistributionStrategy)
                    .customDistribution(List.of())
                    .createdAt(now)
                    .updatedAt(now)
                    .isActive(true)
                    .createdBy("system") // System user
                    .updatedBy("system")
                    .defaultSettings(Boolean.TRUE)
                    .build();

            examSettingsRepository.save(defaultSettings);
            log.info("Successfully initialized default client exam settings");

        } catch (Exception e) {
            log.error("Failed to initialize default client exam settings", e);
        }
    }
}
