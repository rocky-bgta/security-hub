package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeStep1Request {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String language;

    @NotNull
    private DeepfakeBackgroundType backgroundType;

    private DeepfakeBackgroundPreset backgroundPreset;

    private String backgroundKey;
}
