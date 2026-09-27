package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.clientAdmin.ClientProductDTO;
import com.aspire.asat.cms.repository.custom.BundleRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BundleRepositoryCustomImpl implements BundleRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Document> getEnrichedBundleDetails(List<ClientProductDTO> clientProductDTOS) {
        if (clientProductDTOS == null || clientProductDTOS.isEmpty()) return List.of();

        List<String> packageIds = clientProductDTOS.stream()
                .map(ClientProductDTO::getPackageId)
                .distinct()
                .toList();

        MatchOperation match = Aggregation.match(Criteria.where("_id").in(packageIds));

        LookupOperation featureLookup = Aggregation.lookup("feature", "featureIds", "_id", "featureDetails");
        LookupOperation productLookup = Aggregation.lookup("package", "packageId", "_id", "productDetails");

        UnwindOperation unwindProduct = Aggregation.unwind("productDetails");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("bundleId")
                .and("bundleName").as("bundleName")
                .and("packageId").as("packageId")
                .and("productDetails.packageName").as("productName")
                .and("productDetails.packageDescription").as("packageDescription")
                .and("featureDetails.featureName").as("featureNames");

        Aggregation aggregation = Aggregation.newAggregation(match, featureLookup, productLookup, unwindProduct, project);

        return mongoTemplate.aggregate(aggregation, "bundles", Document.class).getMappedResults();
    }


}
