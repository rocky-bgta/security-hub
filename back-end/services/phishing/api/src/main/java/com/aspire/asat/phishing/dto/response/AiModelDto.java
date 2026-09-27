package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDto {

    private String id;
    private String name;
    private AiProviderType providerType;
    private boolean isDefault;
    private boolean isActive;
    private Instant createdAt;
}
