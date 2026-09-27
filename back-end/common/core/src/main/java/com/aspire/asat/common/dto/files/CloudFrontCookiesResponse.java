package com.aspire.asat.common.dto.files;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudFrontCookiesResponse {
    private String cloudFrontPolicy;
    private String cloudFrontSignature;
    private String cloudFrontKeyPairId;
}
