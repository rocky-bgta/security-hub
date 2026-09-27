package com.aspire.asat.phishing.dto.sqs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttTranscriptionMessage {
    private UUID audioId;
    private String s3Key;
    private String bucket;
    private String language;
}
