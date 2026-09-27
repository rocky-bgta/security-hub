package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiGlobalConfigRequest;
import com.aspire.asat.phishing.dto.request.AiProviderCredentialsRequest;
import com.aspire.asat.phishing.dto.response.AiGlobalConfigResponse;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.AiProviderSecretRef;
import com.aspire.asat.phishing.repository.AiProviderSecretRefRepository;
import com.aspire.asat.phishing.service.AiConfigService;
import com.aspire.asat.phishing.utils.AiClientAdminIdSupport;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    private final UserCurrentContextService userCurrentContextService;
    private final AiSecretStoreService aiSecretStoreService;
    private final AiProviderSecretRefRepository secretRefRepository;

    @Override
    public AiGlobalConfigResponse saveGlobalConfig(AiGlobalConfigRequest request) {
        if (request == null) {
            throw new ServiceException("AiGlobalConfigRequest is required");
        }
        AiProviderType providerType = request.getProviderType();
        String clientAdminId = AiClientAdminIdSupport.resolve(userCurrentContextService.getCurrentUserContext());

        AiProviderCredentialsRequest creds = AiProviderCredentialsRequest.builder()
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .description(request.getDescription())
                .build();

        aiSecretStoreService.resolveCredentials(providerType, clientAdminId, creds);

        AiProviderSecretRef ref = secretRefRepository
                .findByClientAdminIdAndProviderTypeAndActiveIsTrue(clientAdminId, providerType)
                .orElseThrow(() -> new ServiceException("Failed to read saved AI provider secret reference"));

        return AiGlobalConfigResponse.builder()
                .providerType(providerType)
                .clientAdminId(clientAdminId)
                .apiKeyParameterName(ref.getApiKeyParameterName())
                .apiSecretParameterName(ref.getApiSecretParameterName())
                .build();
    }

    @Override
    public List<AiProviderSecretRef> listAiProviderSecretRefsByClientAdminId() {
        String clientAdminId = AiClientAdminIdSupport.resolve(userCurrentContextService.getCurrentUserContext());
        return secretRefRepository.findByClientAdminIdOrderByProviderTypeAsc(clientAdminId);
    }
}
