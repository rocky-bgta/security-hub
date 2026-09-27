package com.aspire.asat.registration.repository.custom.impl;

import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.repository.custom.MspProductRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MspProductRepositoryCustomImpl implements MspProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<MspProduct> findAllWithFilters(String mspId, String productId, String packageId, String country, String search) {
        // Build criteria for MspProduct (msp_products collection)
        Criteria mspProductCriteria = new Criteria();
        if (mspId != null && !mspId.trim().isEmpty()) {
            mspProductCriteria = mspProductCriteria.and("mspId").is(mspId);
        }
        if (productId != null && !productId.trim().isEmpty()) {
            mspProductCriteria = mspProductCriteria.and("productId").is(productId);
        }
        if (packageId != null && !packageId.trim().isEmpty()) {
            mspProductCriteria = mspProductCriteria.and("packageId").is(packageId);
        }

        // Always aggregate MspProduct with MspUser so filters (country, search by organizationName) apply correctly
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(mspProductCriteria));
        // Join msp_products.mspId to msp_user._id (MspProduct.mspId stores MspUser document id)
        operations.add(Aggregation.lookup("msp_user", "mspId", "_id", "mspUser"));
        operations.add(Aggregation.unwind("mspUser", false));
        operations.add(Aggregation.match(buildMspUserCriteria(country, search)));
        operations.add(Aggregation.project()
                .and("_id").as("id")
                .and("mspId").as("mspId")
                .and("productId").as("productId")
                .and("packageId").as("packageId")
                .and("licenseCount").as("licenseCount")
                .and("usedLicenseCount").as("usedLicenseCount")
                .and("pricePerLicense").as("pricePerLicense")
                .and("totalPrice").as("totalPrice")
                .and("validityPeriod").as("validityPeriod")
                .and("validityUnit").as("validityUnit")
                .and("assignedAt").as("assignedAt")
                .and("expiryDate").as("expiryDate")
                .and("licenseStatus").as("licenseStatus")
                .and("paymentPayload").as("paymentPayload")
                .and("countryId").as("countryId")
        );

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<MspProduct> results = mongoTemplate.aggregate(
                aggregation, "msp_products", MspProduct.class);
        return results.getMappedResults();
    }

    /**
     * Build criteria for MspUser filtering (applied after lookup/unwind on mspUser field).
     * Search is by organizationName only (case-insensitive partial match).
     */
    private Criteria buildMspUserCriteria(String country, String search) {
        List<Criteria> andConditions = new ArrayList<>();

        if (country != null && !country.trim().isEmpty()) {
            andConditions.add(Criteria.where("mspUser.country").is(country));
        }

        if (search != null && !search.trim().isEmpty()) {
            String escaped = search.trim().replaceAll("([.*+?^${}()|\\[\\]\\\\])", "\\\\$1");
            andConditions.add(Criteria.where("mspUser.organizationName").regex(".*" + escaped + ".*", "i"));
        }

        if (andConditions.isEmpty()) {
            return new Criteria();
        }
        return new Criteria().andOperator(andConditions.toArray(new Criteria[0]));
    }
}

