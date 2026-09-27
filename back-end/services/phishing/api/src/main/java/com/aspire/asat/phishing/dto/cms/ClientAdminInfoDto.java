package com.aspire.asat.phishing.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO holding only the ClientAdmin fields needed for topic recommendation filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAdminInfoDto {

    private String country;
    private String complianceId;
    private String industry;
    private String subIndustryId;
}
