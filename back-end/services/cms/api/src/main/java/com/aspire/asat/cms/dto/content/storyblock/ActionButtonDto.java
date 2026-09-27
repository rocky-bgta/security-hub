package com.aspire.asat.cms.dto.content.storyblock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionButtonDto {
    private String buttonTitle;
    private String textColor;
    private String buttonColor;
    private String hoverColor;
}
