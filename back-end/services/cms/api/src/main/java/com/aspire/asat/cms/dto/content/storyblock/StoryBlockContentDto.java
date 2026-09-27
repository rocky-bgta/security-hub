package com.aspire.asat.cms.dto.content.storyblock;

import com.aspire.asat.cms.dto.content.formatting.TextBackgroundSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@Builder
//@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoryBlockContentDto {

    // Steps (Each step has a title, description, and optional formatting)
    private List<StepDto> steps;

    // Pop-up content (configurable)
    private PopUpDto popUp;

    // Advisor Instruction (guidance text)
    private String advisorInstructionText;

    // Action Buttons (configurable)
    private ActionButtonDto actionButton;

    // Correct Situation (Agree/Ignore with alternatives)
    private CorrectSituationDto correctSituation;

    // Background settings (image, color tone, opacity)
    private TextBackgroundSetting backgroundSettings;

    // Accessibility settings (High contrast mode)
    private boolean highContrastModeEnabled;

    private  Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
