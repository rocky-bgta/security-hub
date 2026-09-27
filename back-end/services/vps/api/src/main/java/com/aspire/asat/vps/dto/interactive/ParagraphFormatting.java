package com.aspire.asat.vps.dto.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParagraphFormatting {
    private String paragraph;
    private String paragraphColor;
    private boolean paragraphHighContrastMode;
}
