package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.packageRangePricing.BulkPackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.PackageUserRangePricing;
import com.aspire.asat.cms.model.UserRange;
import com.aspire.asat.cms.repository.PackageUserRangePricingRepository;
import com.aspire.asat.cms.repository.UserRangeRepository;
import com.aspire.asat.cms.service.PackageRangePricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageRangePricingServiceImpl implements PackageRangePricingService {

    private final PackageUserRangePricingRepository pricingRepository;
    private final UserRangeRepository userRangeRepository;

    @Override
    @Transactional
    public PackageRangePricingResponse createPackageRangePricing(PackageRangePricingRequest request, String packageId) {
        log.info("Creating package range pricing for packageId: {}, userRangeId: {}", packageId, request.getUserRangeId());

        // Validate user range exists
        UserRange userRange = userRangeRepository.findByIdAndIsActiveTrue(request.getUserRangeId())
                .orElseThrow(() -> new ResourceNotFoundException("User range not found with id: " + request.getUserRangeId()));

        // Check if pricing already exists
        if (pricingRepository.existsByPackageIdAndUserRangeIdAndIsActiveTrue(packageId, request.getUserRangeId())) {
            throw new IllegalArgumentException("Pricing for this package and range already exists");
        }

        Instant now = Instant.now();
        PackageUserRangePricing pricing = PackageUserRangePricing.builder()
                .id(UUID.randomUUID().toString())
                .packageId(packageId)
                .userRangeId(request.getUserRangeId())
                .pricePerUser(request.getPricePerUser())
                .yearlyPricePerUser(request.getYearlyPricePerUser())
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .build();

        PackageUserRangePricing saved = pricingRepository.save(pricing);
        return mapToResponse(saved, userRange);
    }

    @Override
    public List<PackageRangePricingResponse> getPricingByPackageId(String packageId) {
        List<PackageUserRangePricing> pricingList = pricingRepository.findByPackageIdAndIsActiveTrue(packageId);
        
        return pricingList.stream()
                .map(pricing -> {
                    UserRange userRange = userRangeRepository.findByIdAndIsActiveTrue(pricing.getUserRangeId())
                            .orElse(null);
                    return mapToResponse(pricing, userRange);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PackageRangePricingResponse getPricingByPackageIdAndUserRangeId(String packageId, String userRangeId) {
        log.info("Getting package range pricing for packageId: {} and userRangeId: {}", packageId, userRangeId);

        PackageUserRangePricing pricing = pricingRepository.findByPackageIdAndUserRangeIdAndIsActiveTrue(packageId, userRangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Package range pricing not found for packageId: " + packageId + " and userRangeId: " + userRangeId));

        UserRange userRange = null;
        if(userRangeId != null) {
        userRange = userRangeRepository.findByIdAndIsActiveTrue(pricing.getUserRangeId())
                .orElse(null);
        }

        return mapToResponse(pricing, userRange);
    }

    @Override
    @Transactional
    public PackageRangePricingResponse updatePackageRangePricing(String id, PackageRangePricingRequest request) {
        log.info("Updating package range pricing with id: {}", id);

        PackageUserRangePricing existing = pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package range pricing not found with id: " + id));

        if (!existing.getIsActive()) {
            throw new ResourceNotFoundException("Package range pricing not found with id: " + id);
        }

        // Validate user range exists
        UserRange userRange = userRangeRepository.findByIdAndIsActiveTrue(request.getUserRangeId())
                .orElseThrow(() -> new ResourceNotFoundException("User range not found with id: " + request.getUserRangeId()));

        // Check if another pricing exists with same package and range (if range changed)
        if (!existing.getUserRangeId().equals(request.getUserRangeId())) {
            if (pricingRepository.existsByPackageIdAndUserRangeIdAndIsActiveTrue(existing.getPackageId(), request.getUserRangeId())) {
                throw new IllegalArgumentException("Pricing for this package and range already exists");
            }
        }

        existing.setUserRangeId(request.getUserRangeId());
        existing.setPricePerUser(request.getPricePerUser());
        existing.setYearlyPricePerUser(request.getYearlyPricePerUser());
        existing.setUpdatedAt(Instant.now());

        PackageUserRangePricing updated = pricingRepository.save(existing);
        return mapToResponse(updated, userRange);
    }

    @Override
    @Transactional
    public void deletePackageRangePricing(String id) {
        log.info("Deleting package range pricing with id: {}", id);

        PackageUserRangePricing pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package range pricing not found with id: " + id));

        // Soft delete
        pricing.setIsActive(false);
        pricing.setUpdatedAt(Instant.now());
        pricingRepository.save(pricing);
    }

    @Override
    @Transactional
    public List<PackageRangePricingResponse> bulkCreateOrUpdatePackageRangePricing(BulkPackageRangePricingRequest request) {
        log.info("Bulk creating/updating package range pricing for packageId: {}", request.getPackageId());

        List<PackageRangePricingResponse> responses = new ArrayList<>();

        for (PackageRangePricingRequest pricingRequest : request.getRangePricing()) {
            // Check if pricing already exists
            var existing = pricingRepository.findByPackageIdAndUserRangeIdAndIsActiveTrue(
                    request.getPackageId(),
                    pricingRequest.getUserRangeId()
            );

            if (existing.isPresent()) {
                // Update existing
                PackageRangePricingResponse updated = updatePackageRangePricing(existing.get().getId(), pricingRequest);
                responses.add(updated);
            } else {
                // Create new
                PackageRangePricingResponse created = createPackageRangePricing(pricingRequest, request.getPackageId());
                responses.add(created);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public void deletePricingByPackageId(String packageId) {
        log.info("Deleting all pricing for packageId: {}", packageId);
        pricingRepository.deleteByPackageId(packageId);
    }

    private PackageRangePricingResponse mapToResponse(PackageUserRangePricing pricing, UserRange userRange) {
        PackageRangePricingResponse.PackageRangePricingResponseBuilder builder = PackageRangePricingResponse.builder()
                .id(pricing.getId())
                .packageId(pricing.getPackageId())
                .userRangeId(pricing.getUserRangeId())
                .pricePerUser(pricing.getPricePerUser())
                .yearlyPricePerUser(pricing.getYearlyPricePerUser())
                .createdAt(pricing.getCreatedAt())
                .updatedAt(pricing.getUpdatedAt())
                .isActive(pricing.getIsActive());

        if (userRange != null) {
            builder.rangeName(userRange.getRangeName())
                    .minUsers(userRange.getMinUsers())
                    .maxUsers(userRange.getMaxUsers());
        }

        return builder.build();
    }
}

