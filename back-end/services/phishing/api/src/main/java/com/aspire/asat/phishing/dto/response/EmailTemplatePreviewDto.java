package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.EmailType;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for full template preview (EMAIL or SMS).
 * Contains complete email HTML or plain-text SMS body for rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplatePreviewDto {
    
    private String templateId;
    
    private String templateName;

    private String description;

    private EmailType emailType;

    private TemplateType templateType;

    private PayloadTypeDto payloadType;

    private DifficultyDto difficultyLevel;

    private String serviceLocation;

    private List<String> tags;

    private String language;

    private int popularity;

    private boolean isPremium;

    private TemplateGenerationType templateGenerationType;

    private EmailTemplateStatus status;
    
    private String emailSubject;

    private String thumbnailUrl;
    
    /**
     * Full HTML email body
     */
    private String emailBody;

    /**
     * Plain-text SMS body (used when templateType is SMS)
     */
    private String smsBody;
    
    /**
     * Plain text version of email body
     */
    private String emailBodyText;
    
    /**
     * Attachment URLs
     */
    private List<String> attachments;
}
