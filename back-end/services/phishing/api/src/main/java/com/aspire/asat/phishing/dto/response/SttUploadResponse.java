package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttUploadResponse {
    private UUID audioId;
    private TranscriptionStatus status;
}
