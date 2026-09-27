package com.aspire.asat.cms.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for exporting certificates to Excel and sending via email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateExportRequestDto {

    @NotBlank(message = "Client admin ID is required")
    private String adminId;

    @NotEmpty(message = "Certificate IDs list cannot be empty")
    private List<String> certificateIds;
}

