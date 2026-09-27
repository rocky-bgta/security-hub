package com.aspire.asat.cms.config;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Configuration class for topic enabling/disabling logic based on duration and question count.
 * <p>
 * Business Rules:
 * - Topics are DISABLED by default when created
 * - If duration is ≤ short-threshold minutes: requires at least short-duration questions to be ENABLED
 * - If duration is > long-threshold minutes: requires at least long-duration questions to be ENABLED
 * - If duration is between short-threshold and long-threshold minutes: requires at least short-duration questions to be ENABLED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TopicStatusConfig {
    
    private final TopicStatusProperties properties;
    
    // Default status for new topics
    public static final TopicStatus DEFAULT_TOPIC_STATUS = TopicStatus.DISABLED;

    /**
     * Determines the required number of questions for a topic based on its duration.
     *
     * @param durationMinutes the duration of the topic in minutes
     * @return the minimum number of questions required for the topic to be enabled
     */
    public int getRequiredQuestionCount(Integer durationMinutes) {
        if (durationMinutes == null) {
            log.debug("Duration is null, returning 0 required questions");
            return 0;
        }

        int shortThreshold = properties.getDuration().getShortThreshold();
        int longThreshold = properties.getDuration().getLongThreshold();
        int shortDurationQuestions = properties.getRequiredQuestions().getShortDuration();
        int longDurationQuestions = properties.getRequiredQuestions().getLongDuration();

        if (durationMinutes <= shortThreshold) {
            log.debug("Duration {} minutes <= {}, requires {} questions",
                    durationMinutes, shortThreshold, shortDurationQuestions);
            return shortDurationQuestions;
        } else if (durationMinutes > longThreshold) {
            log.debug("Duration {} minutes > {}, requires {} questions",
                    durationMinutes, longThreshold, longDurationQuestions);
            return longDurationQuestions;
        } else {
            // Duration between short-threshold and long-threshold minutes (exclusive)
            log.debug("Duration {} minutes between {} and {}, requires {} questions",
                    durationMinutes, shortThreshold, longThreshold, shortDurationQuestions);
            return shortDurationQuestions;
        }
    }

    /**
     * Determines if a topic should be enabled based on its duration and question count.
     *
     * @param durationMinutes the duration of the topic in minutes
     * @param questionCount   the current number of active questions for the topic
     * @return true if the topic should be enabled, false otherwise
     */
    public boolean shouldTopicBeEnabled(Integer durationMinutes, long questionCount) {
        if (durationMinutes == null) {
            log.debug("Duration is null, topic should remain disabled");
            return false;
        }

        int requiredQuestions = getRequiredQuestionCount(durationMinutes);
        boolean shouldEnable = questionCount >= requiredQuestions;

        log.debug("Topic with duration {} minutes and {} questions: required={}, shouldEnable={}",
                durationMinutes, questionCount, requiredQuestions, shouldEnable);

        return shouldEnable;
    }

    /**
     * Determines the appropriate status for a topic based on its duration and question count.
     *
     * @param durationMinutes the duration of the topic in minutes
     * @param questionCount   the current number of active questions for the topic
     * @return the appropriate TopicStatus (ENABLED or DISABLED)
     */
    public TopicStatus determineTopicStatus(Integer durationMinutes, long questionCount) {
        boolean shouldEnable = shouldTopicBeEnabled(durationMinutes, questionCount);
        TopicStatus status = shouldEnable ? TopicStatus.ENABLED : TopicStatus.DISABLED;

        log.debug("Determined topic status: {} (duration: {} minutes, questions: {})",
                status, durationMinutes, questionCount);

        return status;
    }

    /**
     * Gets a human-readable description of the topic enabling criteria.
     *
     * @param durationMinutes the duration of the topic in minutes
     * @return a description string explaining the criteria
     */
    public String getCriteriaDescription(Integer durationMinutes) {
        if (durationMinutes == null) {
            return "Topic has no duration set - will remain disabled";
        }

        int requiredQuestions = getRequiredQuestionCount(durationMinutes);
        int shortThreshold = properties.getDuration().getShortThreshold();
        int longThreshold = properties.getDuration().getLongThreshold();

        if (durationMinutes <= shortThreshold) {
            return String.format("Duration %d minutes (≤%d minutes): requires at least %d questions to enable",
                    durationMinutes, shortThreshold, requiredQuestions);
        } else if (durationMinutes > longThreshold) {
            return String.format("Duration %d minutes (>%d minutes): requires at least %d questions to enable",
                    durationMinutes, longThreshold, requiredQuestions);
        } else {
            return String.format("Duration %d minutes (%d-%d minutes): requires at least %d questions to enable",
                    durationMinutes, shortThreshold + 1, longThreshold, requiredQuestions);
        }
    }
}
