package com.aspire.asat.cms.dto.content.storyblock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectSituationDto {
    private String agreeText;     // Text to display for Agree option
    private String ignoreText;    // Text to display for Ignore option
}
