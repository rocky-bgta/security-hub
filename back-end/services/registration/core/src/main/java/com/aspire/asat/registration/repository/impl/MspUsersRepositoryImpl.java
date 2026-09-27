package com.aspire.asat.registration.repository.impl;

import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.custom.MspUsersRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MspUsersRepositoryImpl implements MspUsersRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<MspUser> findMspUsersWithFilters(
            String search,
            MspStatus status,
            Pageable pageable) {

        log.info("Finding MSP users with filters - search: {}, status: {}, page: {}, size: {}",
                search, status, pageable.getPageNumber(), pageable.getPageSize());

        Query query = buildFilterQuery(search, status, null, null);
        
        // Apply pagination
        query.with(pageable);
        
        // Apply sorting by createdAt descending (newest first)
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<MspUser> mspUsers = mongoTemplate.find(query, MspUser.class);
        long total = mongoTemplate.count(Query.of(query).limit(0).skip(0), MspUser.class);

        log.info("Found {} MSP users out of {} total", mspUsers.size(), total);

        return new PageImpl<>(mspUsers, pageable, total);
    }

    @Override
    public Page<MspUser> findMspUsersWithFilters(
            String search,
            MspStatus status,
            String mspTier,
            String country,
            Pageable pageable) {

        log.info("Finding MSP users with filters - search: {}, status: {}, mspTier: {}, country: {}, page: {}, size: {}",
                search, status, mspTier, country, pageable.getPageNumber(), pageable.getPageSize());

        Query query = buildFilterQuery(search, status, mspTier, country);
        
        // Apply pagination
        query.with(pageable);
        
        // Apply sorting by createdAt descending (newest first)
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<MspUser> mspUsers = mongoTemplate.find(query, MspUser.class);
        long total = mongoTemplate.count(Query.of(query).limit(0).skip(0), MspUser.class);

        log.info("Found {} MSP users out of {} total", mspUsers.size(), total);

        return new PageImpl<>(mspUsers, pageable, total);
    }

    private Query buildFilterQuery(String search, MspStatus status, String mspTier, String country) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Search filter - search only in organizationName (case-insensitive regex)
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = ".*" + search.trim() + ".*";
            criteriaList.add(Criteria.where("organizationName").regex(searchPattern, "i"));
        }

        // Status filter
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status.name()));
        }

        // MSP Tier filter (exact match)
        if (mspTier != null && !mspTier.trim().isEmpty()) {
            criteriaList.add(Criteria.where("mspTier").is(mspTier.trim()));
        }

        // Country filter (exact match)
        if (country != null && !country.trim().isEmpty()) {
            criteriaList.add(Criteria.where("country").is(country.trim()));
        }

        // Combine all criteria with AND
        if (!criteriaList.isEmpty()) {
            Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(finalCriteria);
        }

        return query;
    }

    @Override
    public OrganizationLicenseStatistics getOrganizationLicenseStatisticsByMspId(String mspId) {
        Instant now = Instant.now();

        // Get all MSP products for this MSP
        List<MspProduct> allProducts = mongoTemplate.find(
                new Query(Criteria.where("mspId").is(mspId)),
                MspProduct.class
        );

        // Initialize counters
        int totalLicenseCount = 0; // Sum of all licenseCount
        int totalAllocatedLicenses = 0; // Sum of all usedLicenseCount
        int totalExpiredLicenses = 0; // Sum of (licenseCount - usedLicenseCount) for expired records only

        // Calculate statistics
        for (MspProduct product : allProducts) {
            int licenseCount = product.getLicenseCount();
            int usedLicenseCount = product.getUsedLicenseCount();

            totalLicenseCount += licenseCount;
            totalAllocatedLicenses += usedLicenseCount;

            // Check if product is expired (expiryDate < current time)
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(now)) {
                // For expired products, calculate unused licenses (licenseCount - usedLicenseCount)
                int unusedLicenses = licenseCount - usedLicenseCount;
                totalExpiredLicenses += unusedLicenses;
            }
        }

        // Calculate active and available licenses
        // Total Active = Total Allocated - Total Expired
        int totalActiveLicenses = totalAllocatedLicenses - totalExpiredLicenses;

        // Total Available = Total License Count - Total Allocated - Total Expired
        int totalAvailableLicenses = totalLicenseCount - totalAllocatedLicenses - totalExpiredLicenses;

        return new OrganizationLicenseStatistics(
                totalAvailableLicenses,
                totalAllocatedLicenses,
                totalActiveLicenses,
                totalExpiredLicenses
        );
    }
}

