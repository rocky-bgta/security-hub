package com.aspire.asat.cms.dto.certificateTemplate;

import com.aspire.asat.cms.dto.enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTemplateReqDto {

    @NotBlank(message = "Template name is required")
    private String templateName;

    private String certificateTitle;
    private String certificateType;
    private String acknowledgement;
    private String completionStatus;
    private String completionTitle;
    private String logoImageUrl;
    private String signatureImageUrl;
    private String signerName;
    private String signerDesignation;
    private String signatureIdentity;
    private String backgroundImageUrl;

    private DynamicFieldsDto dynamicFields;

    private Status status;

    @NotNull(message = "isDefault flag is required")
    private Boolean isDefault;

    private Boolean isTrialTemplate;
}

