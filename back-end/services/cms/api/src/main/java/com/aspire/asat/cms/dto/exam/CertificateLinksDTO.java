package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateLinksDTO {
    private String pdfLink;
    private String imageLink;

    private String productName;
}
