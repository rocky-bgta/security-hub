package com.aspire.asat.cms.dto.certificateTemplate;

import com.aspire.asat.cms.dto.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAdminCertificateTemplateRespDto {
    private String id;

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

    private String createdBy;

    private Instant createdAt;

    private String updatedBy;

    private Instant updatedAt;

    private Boolean isDefault = false;
    private Boolean isAssigned ;
    private Boolean isTrialTemplate = false;
}
