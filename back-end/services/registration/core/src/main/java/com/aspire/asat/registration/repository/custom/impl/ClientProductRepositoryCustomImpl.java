package com.aspire.asat.registration.repository.custom.impl;

import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClientProductRepositoryCustomImpl implements ClientProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ClientProduct> findClientProductsByClientAdminIdWithFilters(String clientAdminId, String search, Pageable pageable) {
        // Build query criteria
        Criteria criteria = Criteria.where("clientAdminId").is(clientAdminId);

        // Note: Search by productName and packageName will be handled in the service layer
        // since we need to fetch product/package details from CMS service first
        // For now, we'll do basic search on available fields
        
        // Build query
        Query query = new Query(criteria);
        
        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<ClientProduct> clientProducts = mongoTemplate.find(query, ClientProduct.class);

        // Count total elements for pagination
        Query countQuery = new Query(criteria);
        long totalElements = mongoTemplate.count(countQuery, ClientProduct.class);

        // Create page
        return PageableExecutionUtils.getPage(clientProducts, pageable, () -> totalElements);
    }

    @Override
    public LicenseStatistics getLicenseStatisticsByClientAdminId(String clientAdminId, String productId) {
        // Only count ACTIVE products - PENDING products should not be included in statistics
        Criteria criteria = Criteria.where("clientAdminId").is(clientAdminId)
                .and("licenseStatus").is("ACTIVE");
        if (productId != null && !productId.isBlank()) {
            criteria = criteria.and("productId").is(productId);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .sum("licenseCount").as("totalLicenseCount")
                        .sum("usedLicenseCount").as("totalUsedLicenseCount")
        );

        AggregationResults<LicenseStatistics> results = mongoTemplate.aggregate(
                aggregation,
                "client_products",
                LicenseStatistics.class
        );

        List<LicenseStatistics> statistics = results.getMappedResults();

        if (statistics.isEmpty()) {
            return new LicenseStatistics(0, 0);
        }

        return statistics.get(0);
    }

    @Override
    public int getUniqueProductCountByClientAdminId(String clientAdminId) {
        // Use distinct to get unique product IDs for ACTIVE products only
        // PENDING products should not be counted
        List<String> uniqueProductIds = mongoTemplate.findDistinct(
                new Query(Criteria.where("clientAdminId").is(clientAdminId)
                        .and("licenseStatus").is("ACTIVE")),
                "productId",
                "client_products",
                String.class
        );
        
        return uniqueProductIds.size();
    }

    @Override
    public OrganizationLicenseStatistics getOrganizationLicenseStatisticsByClientAdminId(String clientAdminId) {
        Instant now = Instant.now();

        // Only get ACTIVE client products - PENDING products should not be included in statistics
        List<ClientProduct> allProducts = mongoTemplate.find(
                new Query(Criteria.where("clientAdminId").is(clientAdminId)
                        .and("licenseStatus").is("ACTIVE")),
                ClientProduct.class
        );

        // Initialize counters
        int totalLicenseCount = 0; // Sum of all licenseCount
        int totalAllocatedLicenses = 0; // Sum of all usedLicenseCount
        int totalExpiredLicenses = 0; // Sum of (licenseCount - usedLicenseCount) for expired records only
        int totalActiveLicenses = 0;
        // Calculate statistics
        for (ClientProduct product : allProducts) {
            int licenseCount = product.getLicenseCount();
            int usedLicenseCount = product.getUsedLicenseCount();

            
            totalLicenseCount += licenseCount;
            totalAllocatedLicenses += usedLicenseCount;

            // Check if product is expired (expiryDate < current time)
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(now)) {
                // For expired products, calculate unused licenses (licenseCount - usedLicenseCount)
                int unusedLicenses = licenseCount - usedLicenseCount;
                totalExpiredLicenses += unusedLicenses;
            } else {
                // Used license but not expired is active  - Allocated license but not expired yet
                totalActiveLicenses += usedLicenseCount;
            }
        }
        
        // Total Available = Total License Count - Total Allocated - Total Expired
        int totalAvailableLicenses = totalLicenseCount - totalAllocatedLicenses - totalExpiredLicenses;

        return new OrganizationLicenseStatistics(
                totalAvailableLicenses,
                totalAllocatedLicenses,
                totalActiveLicenses,
                totalExpiredLicenses
        );
    }

    @Override
    public List<ClientProduct> findAllWithFilters(String clientAdminId, String productId, String packageId, String country, String mspId, String search) {
        // Build criteria for ClientProduct (client_products collection)
        Criteria clientProductCriteria = new Criteria();
        if (clientAdminId != null && !clientAdminId.trim().isEmpty()) {
            clientProductCriteria = clientProductCriteria.and("clientAdminId").is(clientAdminId);
        }
        if (productId != null && !productId.trim().isEmpty()) {
            clientProductCriteria = clientProductCriteria.and("productId").is(productId);
        }
        if (packageId != null && !packageId.trim().isEmpty()) {
            clientProductCriteria = clientProductCriteria.and("packageId").is(packageId);
        }

        List<AggregationOperation> operations = new ArrayList<>();
        // 1. Match ClientProduct filters (empty criteria = match all)
        operations.add(Aggregation.match(clientProductCriteria));
        // 2. Lookup ClientAdmin (use _id as foreign field - MongoDB stores entity id as _id)
        operations.add(Aggregation.lookup("client_admins", "clientAdminId", "_id", "clientAdmin"));
        // 3. Unwind clientAdmin array (preserve documents with no match for consistent filtering)
        operations.add(Aggregation.unwind("clientAdmin", false));
        // 4. Match ClientAdmin filters (country, mspId, organizationName search)
        Criteria clientAdminCriteria = buildClientAdminCriteria(country, mspId, search);
        operations.add(Aggregation.match(clientAdminCriteria));
        // 5. Project back to ClientProduct structure including mspId and countryId
        operations.add(Aggregation.project()
                .and("_id").as("id")
                .and("clientAdminId").as("clientAdminId")
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
                .and("mspId").as("mspId")
                .and("countryId").as("countryId")
        );

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<ClientProduct> results = mongoTemplate.aggregate(
                aggregation, "client_products", ClientProduct.class);
        return results.getMappedResults();
    }

    @Override
    public List<ClientProduct> findSubscriptionsForReport(String clientAdminId, String mspId, Instant assignedFrom, Instant assignedToExclusive) {
        List<Criteria> conditions = new ArrayList<>();

        if (clientAdminId != null && !clientAdminId.trim().isEmpty()) {
            conditions.add(Criteria.where("clientAdminId").is(clientAdminId.trim()));
        }
        if (mspId != null && !mspId.trim().isEmpty()) {
            conditions.add(Criteria.where("mspId").is(mspId.trim()));
        }
        if (assignedFrom != null) {
            conditions.add(Criteria.where("assignedAt").gte(assignedFrom));
        }
        if (assignedToExclusive != null) {
            conditions.add(Criteria.where("assignedAt").lt(assignedToExclusive));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        }
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "assignedAt"));

        return mongoTemplate.find(query, ClientProduct.class);
    }

    /**
     * Build criteria for ClientAdmin filtering (applied after lookup/unwind on clientAdmin field).
     * Supports country/countryId (matches country or countryCode), mspId, and search by organizationName.
     */
    private Criteria buildClientAdminCriteria(String country, String mspId, String search) {
        List<Criteria> andConditions = new ArrayList<>();

        if (country != null && !country.trim().isEmpty()) {
            andConditions.add(new Criteria().orOperator(
                    Criteria.where("clientAdmin.country").is(country)
            ));
        }
        if (mspId != null && !mspId.trim().isEmpty()) {
            andConditions.add(Criteria.where("clientAdmin.mspId").is(mspId));
        }
        if (search != null && !search.trim().isEmpty()) {
            String escaped = search.trim().replaceAll("([.*+?^${}()|\\[\\]\\\\])", "\\\\$1");
            andConditions.add(Criteria.where("clientAdmin.organizationName").regex(".*" + escaped + ".*", "i"));
        }

        if (andConditions.isEmpty()) {
            return new Criteria();
        }
        return new Criteria().andOperator(andConditions.toArray(new Criteria[0]));
    }
}
