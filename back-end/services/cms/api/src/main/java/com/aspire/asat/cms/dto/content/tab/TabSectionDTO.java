package com.aspire.asat.cms.dto.content.tab;

import com.aspire.asat.cms.dto.content.formatting.ParagraphFormatting;
import com.aspire.asat.cms.dto.content.formatting.SubTitleFormatting;
import com.aspire.asat.cms.dto.content.formatting.TextBackgroundSetting;
import com.aspire.asat.cms.dto.content.formatting.TitleFormatting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabSectionDTO {

    private TitleFormatting titleFormatting;

    private SubTitleFormatting subTitleFormatting;

    private ParagraphFormatting paragraphFormatting;

    private TextBackgroundSetting textBackgroundSettings;

    private String url;

    private boolean highContrastModeEnabled;

    private Map<String, String> additionalProperties;

    private Map<String, Object> metadata;
}
