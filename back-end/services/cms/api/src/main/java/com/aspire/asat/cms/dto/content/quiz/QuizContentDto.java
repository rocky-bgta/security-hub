package com.aspire.asat.cms.dto.content.quiz;

import com.aspire.asat.cms.dto.content.formatting.TextBackgroundSetting;
import com.aspire.asat.cms.dto.content.formatting.TitleFormatting;
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
public class QuizContentDto {
    private TitleFormatting titleFormatting;

    private ScoringMode scoringMode;

    private TextBackgroundSetting textBackgroundSettings;

    private List<QuizQuestionDto> questions;

    private Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
