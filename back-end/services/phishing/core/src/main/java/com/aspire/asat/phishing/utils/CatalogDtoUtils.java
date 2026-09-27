package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DepartmentDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;

/**
 * Helpers for embedded catalog DTOs stored on email templates.
 */
public final class CatalogDtoUtils {

    private CatalogDtoUtils() {
    }

    public static String getId(Object catalogDto) {
        if (catalogDto == null) {
            return null;
        }
        if (catalogDto instanceof String) {
            String id = (String) catalogDto;
            return id.isBlank() ? null : id.trim();
        }
        if (catalogDto instanceof PayloadTypeDto) {
            return ((PayloadTypeDto) catalogDto).getId();
        }
        if (catalogDto instanceof DifficultyDto) {
            return ((DifficultyDto) catalogDto).getId();
        }
        if (catalogDto instanceof ToneDto) {
            return ((ToneDto) catalogDto).getId();
        }
        if (catalogDto instanceof TriggerEventDto) {
            return ((TriggerEventDto) catalogDto).getId();
        }
        if (catalogDto instanceof UrgencyLevelDto) {
            return ((UrgencyLevelDto) catalogDto).getId();
        }
        if (catalogDto instanceof SocialEngineeringStrategyDto) {
            return ((SocialEngineeringStrategyDto) catalogDto).getId();
        }
        if (catalogDto instanceof EmotionalTriggerDto) {
            return ((EmotionalTriggerDto) catalogDto).getId();
        }
        if (catalogDto instanceof ExpectedUserActionDto) {
            return ((ExpectedUserActionDto) catalogDto).getId();
        }
        if (catalogDto instanceof CampaignObjectiveDto) {
            return ((CampaignObjectiveDto) catalogDto).getId();
        }
        if (catalogDto instanceof AttackerPersonaDto) {
            return ((AttackerPersonaDto) catalogDto).getId();
        }
        if (catalogDto instanceof AttackTechniqueDto) {
            return ((AttackTechniqueDto) catalogDto).getId();
        }
        if (catalogDto instanceof BrandDto) {
            return ((BrandDto) catalogDto).getId();
        }
        if (catalogDto instanceof CallToActionDto) {
            return ((CallToActionDto) catalogDto).getId();
        }
        if (catalogDto instanceof DepartmentDto) {
            return ((DepartmentDto) catalogDto).getId();
        }
        if (catalogDto instanceof TargetIndustryDto) {
            return ((TargetIndustryDto) catalogDto).getId();
        }
        if (catalogDto instanceof ConstraintsDataDto) {
            return ((ConstraintsDataDto) catalogDto).getId();
        }
        if (catalogDto instanceof DataCaptureTypeDto) {
            return ((DataCaptureTypeDto) catalogDto).getId();
        }
        return null;
    }

    public static String getName(Object catalogDto) {
        if (catalogDto == null) {
            return null;
        }
        if (catalogDto instanceof String) {
            String name = (String) catalogDto;
            return name.isBlank() ? null : name.trim();
        }
        if (catalogDto instanceof PayloadTypeDto) {
            return ((PayloadTypeDto) catalogDto).getName();
        }
        if (catalogDto instanceof DifficultyDto) {
            return ((DifficultyDto) catalogDto).getName();
        }
        if (catalogDto instanceof ToneDto) {
            return ((ToneDto) catalogDto).getName();
        }
        if (catalogDto instanceof TriggerEventDto) {
            return ((TriggerEventDto) catalogDto).getName();
        }
        if (catalogDto instanceof UrgencyLevelDto) {
            return ((UrgencyLevelDto) catalogDto).getName();
        }
        if (catalogDto instanceof SocialEngineeringStrategyDto) {
            return ((SocialEngineeringStrategyDto) catalogDto).getName();
        }
        if (catalogDto instanceof EmotionalTriggerDto) {
            return ((EmotionalTriggerDto) catalogDto).getName();
        }
        if (catalogDto instanceof ExpectedUserActionDto) {
            return ((ExpectedUserActionDto) catalogDto).getName();
        }
        if (catalogDto instanceof CampaignObjectiveDto) {
            return ((CampaignObjectiveDto) catalogDto).getName();
        }
        if (catalogDto instanceof AttackerPersonaDto) {
            return ((AttackerPersonaDto) catalogDto).getName();
        }
        if (catalogDto instanceof AttackTechniqueDto) {
            return ((AttackTechniqueDto) catalogDto).getName();
        }
        if (catalogDto instanceof BrandDto) {
            return ((BrandDto) catalogDto).getName();
        }
        if (catalogDto instanceof CallToActionDto) {
            return ((CallToActionDto) catalogDto).getName();
        }
        if (catalogDto instanceof DepartmentDto) {
            return ((DepartmentDto) catalogDto).getName();
        }
        if (catalogDto instanceof TargetIndustryDto) {
            return ((TargetIndustryDto) catalogDto).getName();
        }
        if (catalogDto instanceof ConstraintsDataDto) {
            return ((ConstraintsDataDto) catalogDto).getName();
        }
        if (catalogDto instanceof DataCaptureTypeDto) {
            return ((DataCaptureTypeDto) catalogDto).getName();
        }
        return null;
    }
}
