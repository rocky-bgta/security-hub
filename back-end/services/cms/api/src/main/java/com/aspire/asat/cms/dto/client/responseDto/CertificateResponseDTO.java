package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponseDTO {
    private String packageId;
    private String productId;
    private String productName;
    private String packageName;
    private String thumbnailUrl;
    private String certificateLink;
    private String imageCertificateLink;
    private Instant createdAt;
}
