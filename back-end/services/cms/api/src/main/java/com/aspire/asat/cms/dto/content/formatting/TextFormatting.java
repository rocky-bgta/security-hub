package com.aspire.asat.cms.dto.content.formatting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextFormatting {

    private String text;
    private String textColor;
    private Boolean titleHighContrastMode;
    private Integer textFontSize;
    private String textFontStyle;
    private String textFontFamily;
    private String textAlignment;
    private String textTransform;
    private String textFontWeight;
}
