package com.aspire.asat.phishing.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResolvedCredentials {
    private String apiKey;
    private String apiSecret;
}

