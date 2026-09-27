package com.aspire.asat.cms.dto.certificateTemplate;

import com.aspire.asat.cms.dto.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTemplateStatusUpdateDto {

    @NotNull(message = "Status is required")
    private Status status;
}

