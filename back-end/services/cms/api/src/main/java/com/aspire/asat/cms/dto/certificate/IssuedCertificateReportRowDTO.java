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
public class IssuedCertificateReportRowDTO {
    private String certificateId;
    private String user;
    private String course;
    private Instant issued;
    private Instant expiry;
    private String issuedBy;
    private String status;
}
