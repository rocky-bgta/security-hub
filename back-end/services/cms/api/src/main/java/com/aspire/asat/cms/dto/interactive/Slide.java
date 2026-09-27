package com.aspire.asat.cms.dto.interactive;

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
public class Slide {
    private TitleFormatting titleFormatting;
    private SubTitleFormatting subTitleFormatting;
    private ParagraphFormatting paragraphFormatting;
    private String featureImageLink;
    private String id;
}
