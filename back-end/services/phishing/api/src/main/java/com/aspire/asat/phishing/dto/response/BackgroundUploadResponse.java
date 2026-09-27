package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundUploadResponse {
    /** Persisted background image identifier. */
    private UUID id;
    private String backgroundKey;
    private String backgroundPreviewUrl;
}
