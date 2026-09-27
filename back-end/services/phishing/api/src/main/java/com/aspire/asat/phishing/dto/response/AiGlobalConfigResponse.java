package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirms persisted AI config metadata (no raw secrets).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGlobalConfigResponse {

    private AiProviderType providerType;
    private String clientAdminId;
    private String apiKeyParameterName;
    private String apiSecretParameterName;
}
