package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.util.TrainingCompletionStatusResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB entity for campaign recipients.
 * Tracks individual recipient status and interactions.
 */
@Document(collection = "campaign_recipients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "campaign_user_idx", def = "{'campaignId': 1, 'userId': 1}", unique = true),
    @CompoundIndex(name = "campaign_status_idx", def = "{'campaignId': 1, 'status': 1}"),
    @CompoundIndex(name = "tracking_idx", def = "{'trackingId': 1}", unique = true)
})
public class CampaignRecipient {

    @Id
    private String id;

    @Indexed
    private String campaignId;

    private String campaignName;

    private String clientId;

    private String userId;           // From registration service

    private String email;

    private String firstName;

    private String lastName;

    private String department;

    /** Registration user's organization name; used for {{organizationName}} in templates. */
    private String organizationName;

    /** Registration user's organization domain; used for {{organizationDomain}}/{{domain}} in templates. */
    private String organizationDomain;

    /** Registration user's phone; used for {{PHONE_NUMBER}} in templates. */
    private String phoneNumber;

    /** Registration user's country (display name); used for {{LOCATION}} in templates. */
    private String countryName;

    @Builder.Default
    private RecipientStatus status = RecipientStatus.PENDING;

    // Tracking timestamps
    private Instant emailSentAt;
    private Instant emailDeliveredAt;
    private Instant smsSentAt;
    private Instant smsDeliveredAt;
    private Instant emailOpenedAt;
    private Instant linkClickedAt;
    private Instant dataSubmittedAt;
    private Instant reportedAt;
    private Instant bouncedAt;
    private Instant callQueuedAt;
    private Instant callAnsweredAt;
    private Instant callCompromisedAt;
    private Instant callFailedAt;

    // Unique tracking token for this recipient
    @Indexed(unique = true)
    private String trackingId;

    // Captured data (if user submitted form)
    @Builder.Default
    private Map<String, Object> submittedData = new HashMap<>();

    // Additional tracking metadata
    private String userAgent;
    private String ipAddress;
    private int openCount;
    private int clickCount;

    /** Risk score for this recipient (0–100) based on email activity. */
    @Builder.Default
    private Double riskScore = 0.0;

    // --- Training assignment tracking ---

    /**
     * True once a training sub-package has been assigned to this recipient
     * (PHISHING_WITH_TRAINING campaigns, triggered by click/compromise per assignedFor policy).
     * Defines the denominator for campaign training-completion.
     */
    @Builder.Default
    private boolean trainingAssigned = false;

    /** When the training sub-package was assigned to this recipient. */
    private Instant trainingAssignedAt;

    /**
     * Latest CMS training status for this recipient (dashboard {@code complete} from the details API,
     * or raw CMS values such as {@code COMPLETED} / {@code PHISHING_TRAINING_COMPLETED}).
     */
    private String trainingStatus;

    /** When this recipient's training was first observed as completed. */
    private Instant trainingCompletedAt;

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : (lastName != null ? lastName : email);
    }

    /**
     * Check if recipient has interacted with email
     */
    public boolean hasInteracted() {
        return status != RecipientStatus.PENDING && status != RecipientStatus.SENT;
    }

    /**
     * Check if recipient has been compromised (submitted data)
     */
    public boolean isCompromised() {
        return status == RecipientStatus.DATA_SUBMITTED || status == RecipientStatus.COMPROMISED;
    }

    public boolean satisfiesVoiceSimulatedCompletionCriteria() {
        if (status == RecipientStatus.CALL_FAILED || status == RecipientStatus.NO_ANSWER) {
            return true;
        }
        return status == RecipientStatus.ANSWERED
                || status == RecipientStatus.VOICE_ENGAGED
                || status == RecipientStatus.COMPROMISED
                || status == RecipientStatus.REPORTED;
    }

    /**
     * Whether this recipient satisfies simulated-campaign completion criteria:
     * a qualifying engagement activity or a terminal bounce.
     */
    public boolean satisfiesSimulatedCompletionCriteria() {
        if (status == RecipientStatus.BOUNCED) {
            return true;
        }
        return status == RecipientStatus.OPENED
                || status == RecipientStatus.CLICKED
                || status == RecipientStatus.DATA_SUBMITTED
                || status == RecipientStatus.REPORTED;
    }

    /**
     * Whether this recipient's training is completed (dashboard or raw CMS completion status).
     */
    public boolean isTrainingCompleted() {
        return TrainingCompletionStatusResolver.isCompleted(trainingStatus);
    }
}
