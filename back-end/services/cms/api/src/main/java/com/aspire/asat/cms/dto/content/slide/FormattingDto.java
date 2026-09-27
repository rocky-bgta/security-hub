package com.aspire.asat.cms.dto.content.slide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormattingDto {
    private String frontColor;
    private boolean resetFormatting;
}
