package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelUpdateRequest {

    @NotBlank(message = "name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "providerType is required")
    private AiProviderType providerType;
}
