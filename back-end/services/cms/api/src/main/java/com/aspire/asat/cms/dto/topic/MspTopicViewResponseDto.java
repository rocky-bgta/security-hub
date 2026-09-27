package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Topic details for MSP view: metadata plus all product packages with purchase flags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspTopicViewResponseDto {
    private String id;
    private String topicName;
    private List<String> categories;
    private List<String> availableCountries;
    private String productName;
    private String contentType;
    private Integer duration;
    private List<String> difficultyLevel;
    private List<MspPackagePurchaseStatusDto> packages;
}
