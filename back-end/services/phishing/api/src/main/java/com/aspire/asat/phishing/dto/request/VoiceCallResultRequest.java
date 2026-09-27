package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallResultRequest {

    private VishingCallOutcome outcome;
    private int durationSeconds;
    private int retries;
    private String recordingS3Key;
    private String transcript;
    private String audioId;
    @Builder.Default
    private List<String> detectedKeywords = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> sensitiveDataCaptured = new HashMap<>();
    private Double sttLatencyMs;
    private Double llmLatencyMs;
    private Double ttsLatencyMs;
    private Instant startedAt;
    private Instant endedAt;
}
