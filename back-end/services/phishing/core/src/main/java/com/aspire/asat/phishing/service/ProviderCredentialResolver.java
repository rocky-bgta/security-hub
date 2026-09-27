package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;

/**
 * Resolves ready-to-use, decrypted credentials for a provider call.
 *
 * <p>Resolution order: an active per-client {@code ProviderCredential} record,
 * falling back to the platform YAML defaults when the client has not configured
 * their own. This runs in both synchronous (request-scoped) and asynchronous
 * (render pipeline) contexts, so the caller must pass an explicit {@code clientId}.
 */
public interface ProviderCredentialResolver {

    /**
     * @param clientId     owning client (from context or the render job)
     * @param providerName provider identifier, e.g. {@code ELEVENLABS}/{@code FISH_AUDIO}/{@code HEYGEN}
     * @return resolved credentials (may have a blank API key when nothing is configured)
     */
    ResolvedProviderCredentials resolve(String clientId, String providerName);

    /**
     * Resolve an explicit per-client credential by document id. Does not fall back to YAML.
     *
     * @param clientId   owning client
     * @param providerId {@link com.aspire.asat.phishing.model.ProviderCredential} id
     * @return decrypted credentials from the DB record
     */
    ResolvedProviderCredentials resolveById(String clientId, String providerId);
}
