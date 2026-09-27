package com.aspire.asat.cms.dto.clientCertificateTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCertificateTemplateRespDto {

    private String id;

    private String templateId;

    private String clientAdminId;

    private boolean active;

    private String createdBy;

    private Instant createdAt;

    private String updatedBy;

    private Instant updatedAt;
}

