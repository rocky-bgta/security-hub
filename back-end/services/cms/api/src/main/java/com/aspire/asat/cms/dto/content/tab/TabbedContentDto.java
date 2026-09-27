package com.aspire.asat.cms.dto.content.tab;

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
public class TabbedContentDto {

    private TitleFormatting titleFormatting;

    private SubTitleFormatting subTitleFormatting;

    private ParagraphFormatting paragraphFormatting;

    private TextBackgroundSetting textBackgroundSettings;

    private List<TabSectionDTO> tabSections;

    private Map<String, String> additionalProperties;

    private Map<String, Object> metadata;
}
