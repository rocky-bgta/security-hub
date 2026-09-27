package com.aspire.asat.cms.dto.content.formatting;

import com.aspire.asat.cms.dto.enums.Tone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundFormatting {

    private String backgroundColor;
    private String backgroundImage;
    private Double backgroundOpacity;
    private Tone tone;

}
