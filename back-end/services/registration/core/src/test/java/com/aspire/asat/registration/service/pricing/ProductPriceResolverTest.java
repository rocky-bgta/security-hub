package com.aspire.asat.registration.service.pricing;

import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.clientAdmin.request.ValidityUnit;
import com.aspire.asat.registration.data.cms.PackageRangePricingResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPriceResolverTest {

    private static final String PRODUCT_ID = "product-1";
    private static final String PACKAGE_ID = "package-1";
    private static final String USER_RANGE_ID = "range-1";

    @Mock
    private CmsServiceClient cmsServiceClient;

    private ProductPriceResolver productPriceResolver;

    @BeforeEach
    void setUp() {
        productPriceResolver = new ProductPriceResolver(cmsServiceClient);
    }

    @Test
    void applyAuthoritativePricing_overwritesTamperedFlatPriceFromCms() {
        ProductSelectionDto selection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(5)
                .pricePerLicense(1.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        stubFlatPackagePrice(50.0);

        productPriceResolver.applyAuthoritativePricing(List.of(selection));

        assertEquals(50.0, selection.getPricePerLicense());
    }

    @Test
    void applyAuthoritativePricing_keepsMatchingFlatPriceUnchanged() {
        ProductSelectionDto selection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(5)
                .pricePerLicense(50.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        stubFlatPackagePrice(50.0);

        productPriceResolver.applyAuthoritativePricing(List.of(selection));

        assertEquals(50.0, selection.getPricePerLicense());
    }

    @Test
    void applyAuthoritativePricing_overwritesTamperedRangePriceFromCms() {
        ProductSelectionDto selection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .userRangeId(USER_RANGE_ID)
                .licenseCount(10)
                .pricePerLicense(1.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        when(cmsServiceClient.getPackageRangePricing(PACKAGE_ID, USER_RANGE_ID))
                .thenReturn(PackageRangePricingResponseDto.builder()
                        .packageId(PACKAGE_ID)
                        .userRangeId(USER_RANGE_ID)
                        .minUsers(1)
                        .maxUsers(100)
                        .pricePerUser(25.0)
                        .build());

        productPriceResolver.applyAuthoritativePricing(List.of(selection));

        assertEquals(25.0, selection.getPricePerLicense());
    }

    @Test
    void applyAuthoritativePricing_usesYearlyRangePriceForYearValidity() {
        ProductSelectionDto selection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .userRangeId(USER_RANGE_ID)
                .licenseCount(10)
                .pricePerLicense(1.0)
                .validityPeriod(1)
                .validityUnit(ValidityUnit.YEAR)
                .build();

        when(cmsServiceClient.getPackageRangePricing(PACKAGE_ID, USER_RANGE_ID))
                .thenReturn(PackageRangePricingResponseDto.builder()
                        .packageId(PACKAGE_ID)
                        .userRangeId(USER_RANGE_ID)
                        .minUsers(1)
                        .maxUsers(100)
                        .pricePerUser(25.0)
                        .yearlyPricePerUser(240.0)
                        .build());

        productPriceResolver.applyAuthoritativePricing(List.of(selection));

        assertEquals(240.0, selection.getPricePerLicense());
    }

    private void stubFlatPackagePrice(double price) {
        CmsPackageDto cmsPackage = CmsPackageDto.builder().id(PACKAGE_ID).price(price).build();
        CmsFullProductResponseDto product = CmsFullProductResponseDto.builder()
                .productId(PRODUCT_ID)
                .packages(List.of(cmsPackage))
                .build();
        when(cmsServiceClient.getFullProductFromCache(PRODUCT_ID)).thenReturn(product);
    }
}
