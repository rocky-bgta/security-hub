package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AiGlobalConfigRequest;
import com.aspire.asat.phishing.dto.response.AiGlobalConfigResponse;
import com.aspire.asat.phishing.model.AiProviderSecretRef;

import java.util.List;

/**
 * Persists tenant-level AI provider credentials (AWS SSM + {@code ai_provider_secret_refs}).
 */
public interface AiConfigService {

    AiGlobalConfigResponse saveGlobalConfig(AiGlobalConfigRequest request);

    /**
     * Lists stored secret metadata for the resolved tenant {@code clientAdminId} (current user context).
     */
    List<AiProviderSecretRef> listAiProviderSecretRefsByClientAdminId();
}
