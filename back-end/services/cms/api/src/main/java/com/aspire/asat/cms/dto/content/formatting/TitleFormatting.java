package com.aspire.asat.cms.dto.content.formatting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TitleFormatting {

    private String title;
    private String titleColor;
    private Boolean titleHighContrastMode;
    private Integer titleFontSize;
    private String titleFontStyle;

}
