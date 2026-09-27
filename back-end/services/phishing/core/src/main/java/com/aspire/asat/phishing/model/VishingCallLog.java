package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "vishing_call_logs")
@CompoundIndex(name = "campaign_recipient_idx", def = "{'campaignId': 1, 'recipientId': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingCallLog {

    @Id
    private String id;

    @Indexed
    private String campaignId;

    private String campaignName;

    private String clientId;

    @Indexed
    private String recipientId;

    @Indexed(unique = true)
    private String trackingId;

    private String recipientName;

    private String phoneNumber;

    private RecipientStatus status;

    private VishingCallOutcome outcome;

    /** Rendered (placeholder-substituted) script spoken to this recipient. */
    private String renderedScript;

    /** S3 key of the pre-synthesized cloned-voice audio played to this recipient. */
    private String audioS3Key;

    private int durationSeconds;

    @Builder.Default
    private int retries = 0;

    private String recordingS3Key;

    private String transcript;

    @Builder.Default
    private List<String> detectedKeywords = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> sensitiveDataCaptured = new HashMap<>();

    private Double sttLatencyMs;

    private Double llmLatencyMs;

    private Double ttsLatencyMs;

    private Instant startedAt;

    private Instant endedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
