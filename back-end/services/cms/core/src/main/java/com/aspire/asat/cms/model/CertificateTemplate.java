package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "certificate_template")
public class CertificateTemplate {

    @Id
    private String id;

    @Indexed
    private String templateName;

    private String certificateTitle;
    private String certificateType;
    private String acknowledgement;
    private String completionStatus;
    private String completionTitle;
    private String  logoImageUrl;
    private String signatureImageUrl;
    private String signerName;
    private String signerDesignation;
    private String signatureIdentity;
    private String backgroundImageUrl;



    private DynamicFields dynamicFields;

    @Indexed
    private Status status;

    private String createdBy;

    private Instant createdAt;

    private String updatedBy;

    private Instant updatedAt;

    private Boolean isDefault;
    private Boolean isTrialTemplate;
}
