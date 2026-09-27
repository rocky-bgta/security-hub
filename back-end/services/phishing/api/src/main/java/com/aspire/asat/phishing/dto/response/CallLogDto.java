package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallLogDto {

    private String id;
    private String recipientId;
    private String recipientName;
    private String phoneNumber;
    private RecipientStatus status;
    private VishingCallOutcome outcome;
    private int durationSeconds;
    private int retries;
    private String recordingS3Key;
    private String transcript;
    private List<String> detectedKeywords;
    private Map<String, Object> sensitiveDataCaptured;
    /** Personalized scenario script; set only when sensitiveDataCaptured is non-empty. */
    private String scenario;
    private Instant startedAt;
    private Instant endedAt;
}
