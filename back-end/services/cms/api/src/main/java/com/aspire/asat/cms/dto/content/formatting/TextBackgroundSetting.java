package com.aspire.asat.cms.dto.content.formatting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextBackgroundSetting {
    private String backgroundColor;
    private String backgroundImageURL;
    private String backgroundImagePosition;
    private double backgroundImageOpacity;
    private String tone;
    private String backgroundSize;
    private String backgroundPositionX;
    private String backgroundPositionY;
}
