package com.aspire.asat.cms.dto.content.slide;


import com.aspire.asat.cms.dto.content.formatting.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlideContentDto {

    private TitleFormatting titleFormatting;

    private SubTitleFormatting subTitleFormatting;

    private ParagraphFormatting paragraphFormatting;

    private TextBackgroundSetting backgroundSettings;

    private List<SlideAttributes> slides;

    private Map<String, String> slideAdditionalProperties;
}
