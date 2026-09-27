package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.SmsServerStatus;
import jakarta.validation.constraints.NotBlank;
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
public class SmsServerConfigurationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Provider is required")
    private String provider;

    private String apiKey;

    private String apiSecret;

    private String senderId;

    private String baseUrl;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private SmsServerStatus status = SmsServerStatus.ACTIVE;

    private Map<String, String> providerMetadata;
}
