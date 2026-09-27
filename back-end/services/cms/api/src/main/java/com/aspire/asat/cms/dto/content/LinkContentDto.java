package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.content.formatting.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LinkContentDto {

    private TitleFormatting titleFormatting;
    private SubTitleFormatting subTitleFormatting;
    private ParagraphFormatting paragraphFormatting;
    private LinkFormatting linkFormatting;
    private BackgroundFormatting backgroundFormatting;
    private String featureImageLink;

}
