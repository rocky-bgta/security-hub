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
public class SlideAdditionalProperties {
    private String buttonText;
    private String buttonTextColor;
    private String buttonColor;
    private String buttonHoverColor;
}
