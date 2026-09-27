package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.packageRangePricing.BulkPackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;

import java.util.List;

public interface PackageRangePricingService {

    PackageRangePricingResponse createPackageRangePricing(PackageRangePricingRequest request, String packageId);

    List<PackageRangePricingResponse> getPricingByPackageId(String packageId);

    PackageRangePricingResponse getPricingByPackageIdAndUserRangeId(String packageId, String userRangeId);

    PackageRangePricingResponse updatePackageRangePricing(String id, PackageRangePricingRequest request);

    void deletePackageRangePricing(String id);

    List<PackageRangePricingResponse> bulkCreateOrUpdatePackageRangePricing(BulkPackageRangePricingRequest request);

    void deletePricingByPackageId(String packageId);
}

