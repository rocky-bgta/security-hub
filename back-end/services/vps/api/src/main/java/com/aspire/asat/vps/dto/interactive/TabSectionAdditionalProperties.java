package com.aspire.asat.vps.dto.interactive;

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
public class TabSectionAdditionalProperties {
    private String paragraph;
    private String navigationButtonText;
    private String displayTime; // in format "00:04"
    private String id;
    private String buttonTextColor;
    private String buttonColor;
    private String buttonHoverColor;
    private String tabbedPosition; // Example: "ROW"
    private String audioUrl;

    //story block
    private String title;
    private String titleColor;
    private String featureImage;
    private boolean openPopup;
    private String popupText;
    private String popupTextColor;
    private String popupButtonText;
    private String popupButtonTextColor;
    private String popupButtonColor;
    private String popupButtonHoverColor;
    private String description;
    private String descriptionColor;
    private String advisorInstructionText;
    private String advisorInstructionTextColor;
    private String actionButtonTitle;
    private String actionButtonTitleColor;
    private String correctSituation;
    private String agreeText;
    private String agreeTextColor;
    private String ignoreText;
    private String ignoreTextColor;

    private String navigationButtonTextColor;
    private String navigationButtonColor;
    private String navigationButtonHoverColor;

    private String acceptanceText;
    private String acceptanceTextColor;
    private String correctAcceptanceText;

}
