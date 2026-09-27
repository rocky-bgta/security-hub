package com.aspire.asat.cms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredCertificateReportRowDTO {
    private String certificateId;
    private String user;
    private String course;
    private Instant expiryDate;
    private String days;
    private String certificateStatus;
}
