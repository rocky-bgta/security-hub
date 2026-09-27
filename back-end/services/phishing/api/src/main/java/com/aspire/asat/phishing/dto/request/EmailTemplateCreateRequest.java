package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.EmailType;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.DepartmentDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new email template manually.
 * Based on BRD Use Case 2.1.3.1: Admin creates email template by filling fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateCreateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    private String templateName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Template status (defaults to ACTIVE for backward compatibility).
     */
    @Builder.Default
    private EmailTemplateStatus status = EmailTemplateStatus.ACTIVE;

    /**
     * Email type - defaults to STANDARD_PHISH (BR-02)
     */
    @Builder.Default
    private EmailType emailType = EmailType.STANDARD_PHISH;

    @Builder.Default
    private TemplateType templateType = TemplateType.EMAIL;
    
    @NotNull(message = "Payload type is required")
    private PayloadTypeDto payloadType;
    
    private String emailSubject;
    
    private String emailBody;

    private String smsBody;
    
    /**
     * Plain text fallback version of email body
     */
    private String emailBodyText;
    
    @NotNull(message = "Difficulty level is required")
    private DifficultyDto difficultyLevel;

    private DepartmentDto department;

    private TargetIndustryDto targetIndustry;

    private ConstraintsDataDto constraints;
    
    /**
     * Country/Region where template is applicable
     */
    private String serviceLocation;

    /**
     * Optional thumbnail URL for template list/preview cards.
     */
    private String thumbnailUrl;

    /**
     * URLs after client-side upload to object storage (max 5).
     */
    @Size(max = 5, message = "Maximum 5 attachments allowed")
    private List<String> attachments;
    
    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<String> tags;
    
    /**
     * Employee data fields required for personalization (BR-03: at least one required)
     */
    @NotEmpty(message = "At least one employee data field is required")
    private List<String> employeeDataRequired;
    
    /**
     * Language of the template content (default: "en")
     */
    @Builder.Default
    private String language = "en";

    /**
     * Optional landing page IDs to bind to this template.
     */
    private List<String> landingPageIds;
}
