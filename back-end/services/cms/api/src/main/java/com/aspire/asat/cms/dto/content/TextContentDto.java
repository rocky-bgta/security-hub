package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.content.formatting.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextContentDto {
    
    private TitleFormatting titleFormatting;

    private SubTitleFormatting subTitleFormatting;

    private ParagraphFormatting paragraphFormatting;

    private TextFormatting textFormatting;

    private TextBackgroundSetting textBackgroundSettings;

    private boolean highContrastModeEnabled;

    private  Map<String, String> additionalProperties;

    private Map<String, Object> metadata;

}
