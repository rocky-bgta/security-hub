package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.PhishProneBucketTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One bucket in the phish-prone users widget (cohort counts by tier).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishProneTierUiDto {
    private PhishProneBucketTier tier;
    private int count;
}
