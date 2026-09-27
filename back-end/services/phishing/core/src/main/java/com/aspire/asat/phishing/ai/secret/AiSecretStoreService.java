package com.aspire.asat.phishing.ai.secret;

import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiProviderCredentialsRequest;

/**
 * Resolves provider credentials from AWS SSM using parameter names stored in MongoDB.
 * When {@code requestCredentials} includes a non-blank {@code apiKey}, values are written to SSM and DB metadata is upserted.
 */
public interface AiSecretStoreService {

    AiResolvedCredentials resolveCredentials(
            AiProviderType providerType,
            String clientAdminId,
            AiProviderCredentialsRequest requestCredentials);
}

