package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceServerConfigurationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Provider is required")
    private VoiceProviderType provider;

    private String apiKey;

    private String apiSecret;

    private String callerId;

    private String baseUrl;

    private String region;

    private String countryCode;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private VoiceServerStatus status = VoiceServerStatus.ACTIVE;

    private Map<String, String> providerMetadata;
}
