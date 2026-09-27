package com.aspire.asat.phishing.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Normalized options passed to prompt builders and provider adapters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationOptions {

    private String tone;
    private String language;
    private String inputLanguage;
    private String difficulty;
    private String difficultyLevel;
    private String brand;
    private String contentLength;
    private String callToAction;
    private String constraints;
    private String targetIndustry;
    private String department;
    private String attackerPersona;
    private String attackTechnique;
    private String triggerEvent;
    private String expectedUserAction;
    private String socialEngineeringStrategy;
    private String campaignObjective;
    private String generationMode;
    private String layoutStyle;
    private String targetDepartment;
    private String urgencyLevel;
    private String emotionalTrigger;
    private String dataCaptureType;
    private String additionalContext;
    private String emailSubject;
    private String description;
    private String voiceInputContent;
    private String voiceInput;
    private String targetUrl;
    private List<String> tags;
    private List<String> employeeDataRequired;
}
