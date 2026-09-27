package com.aspire.asat.cms.dto.clientCertificateTemplate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCertificateTemplateReqDto {

    @NotBlank(message = "Template ID is required")
    private String templateId;
}

