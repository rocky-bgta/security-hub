package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import com.aspire.asat.phishing.repository.custom.CampaignLicenseUsageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Unique licensed users from phishing_user_licence (one row = one seat).
 */
@Repository
@RequiredArgsConstructor
public class CampaignLicenseUsageRepositoryCustomImpl implements CampaignLicenseUsageRepositoryCustom {

    private static final String COLLECTION = "phishing_user_licence";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<CampaignLicenseUsageDto> countUniqueUsersByProductPackageId(
            String clientId, String productPackageId) {
        if (!StringUtils.hasText(clientId)) {
            return List.of();
        }

        Criteria matchCriteria = Criteria.where("clientAdminId").is(clientId.trim())
                .and("userId").nin(null, "");

        if (StringUtils.hasText(productPackageId)) {
            matchCriteria = matchCriteria.and("productPackageId").is(productPackageId.trim());
        } else {
            matchCriteria = matchCriteria.and("productPackageId").nin(null, "");
        }

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(matchCriteria));
        operations.add(Aggregation.group("productPackageId").count().as("uniqueUserCount"));
        operations.add(Aggregation.project()
                .and("_id").as("productPackageId")
                .and("uniqueUserCount").as("uniqueUserCount")
                .andExclude("_id"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<CampaignLicenseUsageDto> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, CampaignLicenseUsageDto.class);
        return results.getMappedResults();
    }
}
