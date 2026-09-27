package com.aspire.asat.cms.dto.license;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseAssignmentExportDto {

    private byte[] content;
    private String filename;
    private String contentType;
}
