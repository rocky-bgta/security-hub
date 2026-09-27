package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.SmsServerStatus;
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
public class SmsServerConfigurationDto {

    private String id;
    private String name;
    private String provider;
    private String apiKeyMasked;
    private String apiSecretMasked;
    private String senderId;
    private String baseUrl;
    private boolean isDefault;
    private SmsServerStatus status;
    private Map<String, String> providerMetadata;
    private Instant createdAt;
    private Instant updatedAt;
}
