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
public class CertificateDetailsResponseDTO {
    private String certificateId;
    private String fullName;
    private String email;
    private String userId;
    private String productName;
    private String subPackageId;

    private String certificateUrl;
    private String certificateImageUrl;

    private String status;
    private Instant expiryDate;
    private Instant createdAt;
}
