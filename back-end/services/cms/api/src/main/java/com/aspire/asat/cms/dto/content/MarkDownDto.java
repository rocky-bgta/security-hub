package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.content.formatting.BackgroundFormatting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MarkDownDto {

private String markdownText;
private String headingColor;
private String textColor;
private String linkColor;
private BackgroundFormatting backgroundFormatting;

}
