package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceServerConfigurationDto {

    private String id;
    private String name;
    private VoiceProviderType provider;
    private String apiKeyMasked;
    private String apiSecretMasked;
    private String callerId;
    private String baseUrl;
    private String region;
    private String countryCode;
    private boolean isDefault;
    private boolean isGlobal;
    private boolean canEdit;
    private VoiceServerStatus status;
    private Map<String, String> providerMetadata;
    private Instant createdAt;
    private Instant updatedAt;
}
