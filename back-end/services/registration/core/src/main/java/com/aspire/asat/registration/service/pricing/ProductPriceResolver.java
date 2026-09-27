package com.aspire.asat.registration.service.pricing;

import com.aspire.asat.common.util.MoneyUtil;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.clientAdmin.request.ValidityUnit;
import com.aspire.asat.registration.data.cms.PackageRangePricingResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.exception.RegistationValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Resolves authoritative unit prices from CMS and overwrites client-supplied {@code pricePerLicense} values.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPriceResolver {

    private final CmsServiceClient cmsServiceClient;

    public void applyAuthoritativePricing(List<ProductSelectionDto> selections) {
        if (selections == null || selections.isEmpty()) {
            return;
        }

        for (ProductSelectionDto selection : selections) {
            Double clientPrice = selection.getPricePerLicense();
            double authoritativePrice = resolveUnitPrice(selection);
            log.info(" Resolved authoritative pricePerLicense {} for productId: {}, packageId: {}, userRangeId: {}",
                    authoritativePrice,
                    selection.getProductId(),
                    selection.getPackageId(),
                    selection.getUserRangeId());

            selection.setPricePerLicense(authoritativePrice);

            if (clientPrice != null && !MoneyUtil.isEqual(clientPrice, authoritativePrice)) {
                log.warn(
                        "Client pricePerLicense {} differs from CMS price {} for productId: {}, packageId: {}, userRangeId: {}",
                        clientPrice,
                        authoritativePrice,
                        selection.getProductId(),
                        selection.getPackageId(),
                        selection.getUserRangeId());
            }
        }
    }

    private double resolveUnitPrice(ProductSelectionDto selection) {
        String packageId = selection.getPackageId();
        String userRangeId = selection.getUserRangeId();
        Integer licenseCount = selection.getLicenseCount();
        ValidityUnit validityUnit = selection.getValidityUnit();

        if (licenseCount == null || licenseCount <= 0) {
            log.error("Invalid license count: {} for packageId: {}", licenseCount, packageId);
            throw new RegistationValidationException(
                    "Invalid license count. License count must be greater than 0 for packageId: " + packageId);
        }

        if (!StringUtils.hasText(userRangeId)) {
            return resolveFlatPackagePrice(selection.getProductId(), packageId);
        }

        return resolveRangePackagePrice(packageId, userRangeId, licenseCount, validityUnit);
    }

    private double resolveFlatPackagePrice(String productId, String packageId) {
        CmsFullProductResponseDto product = cmsServiceClient.getFullProductFromCache(productId);
        if (product == null || product.getPackages() == null) {
            log.error("Product not found in CMS cache for productId: {}", productId);
            throw new RegistationValidationException(
                    "Unable to resolve pricing. Product not found for productId: " + productId);
        }

        CmsPackageDto cmsPackage = product.getPackages().stream()
                .filter(pkg -> packageId != null && packageId.equals(pkg.getId()))
                .findFirst()
                .orElse(null);

        if (cmsPackage == null) {
            log.error("Package not found in CMS cache for productId: {}, packageId: {}", productId, packageId);
            throw new RegistationValidationException(
                    "Unable to resolve pricing. Package not found for packageId: " + packageId);
        }

        Double price = cmsPackage.getPrice();
        if (price == null || price <= 0) {
            log.error("Invalid flat package price for productId: {}, packageId: {}", productId, packageId);
            throw new RegistationValidationException(
                    "Invalid pricing. Price per license must be greater than 0 for packageId: " + packageId);
        }

        return MoneyUtil.round(price);
    }

    private double resolveRangePackagePrice(
            String packageId,
            String userRangeId,
            Integer licenseCount,
            ValidityUnit validityUnit) {
        PackageRangePricingResponseDto pricingResponse =
                cmsServiceClient.getPackageRangePricing(packageId, userRangeId);

        if (pricingResponse == null) {
            log.error("Failed to fetch package range pricing from CMS for packageId: {}, userRangeId: {}",
                    packageId, userRangeId);
            throw new RegistationValidationException(
                    "Unable to resolve pricing. Package range pricing not found for packageId: " + packageId
                            + " and userRangeId: " + userRangeId);
        }

        Integer minUsers = pricingResponse.getMinUsers();
        Integer maxUsers = pricingResponse.getMaxUsers();

        if (minUsers != null && licenseCount < minUsers) {
            log.error("License count {} is below minimum {} for packageId: {}, userRangeId: {}",
                    licenseCount, minUsers, packageId, userRangeId);
            throw new RegistationValidationException(
                    String.format(
                            "License count %d is below the minimum allowed (%d) for the selected user range. "
                                    + "Please select a valid license count.",
                            licenseCount, minUsers));
        }

        if (maxUsers != null && licenseCount > maxUsers) {
            log.error("License count {} exceeds maximum {} for packageId: {}, userRangeId: {}",
                    licenseCount, maxUsers, packageId, userRangeId);
            throw new RegistationValidationException(
                    String.format(
                            "License count %d exceeds the maximum allowed (%d) for the selected user range. "
                                    + "Please select a valid license count or choose a different user range.",
                            licenseCount, maxUsers));
        }

        Double unitPrice;
        if (validityUnit == ValidityUnit.YEAR) {
            unitPrice = pricingResponse.getYearlyPricePerUser();
            if (unitPrice == null) {
                log.error("Yearly price per user is null for packageId: {}, userRangeId: {}", packageId, userRangeId);
                throw new RegistationValidationException(
                        "Invalid pricing configuration. Yearly price per user is not set for packageId: " + packageId);
            }
        } else {
            unitPrice = pricingResponse.getPricePerUser();
            if (unitPrice == null) {
                log.error("Price per user is null for packageId: {}, userRangeId: {}", packageId, userRangeId);
                throw new RegistationValidationException(
                        "Invalid pricing configuration. Price per user is not set for packageId: " + packageId);
            }
        }

        return MoneyUtil.round(unitPrice);
    }
}
