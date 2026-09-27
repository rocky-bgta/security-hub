package com.aspire.asat.cms.dto.content.formatting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ParagraphFormatting {

    private String paragraph;
    private String paragraphColor;
    private Boolean paragraphHighContrastMode;
    private Integer paragraphFontSize;
    private String paragraphFontStyle;


}
