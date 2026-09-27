package com.aspire.asat.cms.dto.content.formatting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SubTitleFormatting {

    private String subtitle;
    private String subtitleColor;
    private Boolean subtitleHighContrastMode;
    private Integer subtitleFontSize;
    private String subtitleFontStyle;

}
