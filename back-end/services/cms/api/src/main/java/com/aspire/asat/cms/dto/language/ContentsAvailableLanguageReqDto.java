package com.aspire.asat.cms.dto.language;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentsAvailableLanguageReqDto {

    @NotBlank(message = "Language name is required")
    private String languageName;

    @NotBlank(message = "Language code is required")
    private String code;

    private boolean active = true;
}
