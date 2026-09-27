package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import org.springframework.data.domain.Page;

/**
 * CRUD + activation/default management for per-client third-party provider
 * credentials used by the deepfake pipeline.
 */
public interface ProviderCredentialService {

    ProviderCredentialDto createProviderCredential(ProviderCredentialCreateRequest request);

    ProviderCredentialDto updateProviderCredential(String id, ProviderCredentialUpdateRequest request);

    void deleteProviderCredential(String id);

    ProviderCredentialDto getProviderCredentialById(String id);

    /**
     * @param providerName optional case-insensitive provider name filter
     * @param isActive     optional active/inactive filter ({@code null} = all)
     */
    Page<ProviderCredentialDto> getProviderCredentials(
            String providerName, Boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    ProviderCredentialDto setDefault(String id);

    ProviderCredentialDto setActive(String id, boolean active);
}
