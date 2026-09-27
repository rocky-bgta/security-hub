package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDiscountResponseDTO;
import com.aspire.asat.billing.dto.ProductRestrictionDTO;
import com.aspire.asat.billing.dto.ValidateCouponRequestDTO;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import com.aspire.asat.billing.enums.CouponType;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.CouponLimitExceededException;
import com.aspire.asat.billing.exception.CouponNotFoundException;
import com.aspire.asat.billing.exception.CouponValidationException;
import com.aspire.asat.billing.model.Coupon;
import com.aspire.asat.billing.repo.CouponRepository;
import com.aspire.asat.billing.repo.customRepo.CouponRepositoryCustom;
import com.aspire.asat.billing.utils.common.QrCodeGenerator;
import com.aspire.asat.billing.utils.file.AzureBlobUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRepositoryCustom couponRepositoryCustom;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AzureBlobUploader azureBlobUploader;

    @Mock
    private QrCodeGenerator qrCodeGenerator;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        couponService.mongoTemplate = mongoTemplate;

        coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("ASAT60");
        coupon.setActive(true);
        coupon.setType(CouponType.PERCENTAGE);
        coupon.setValue(5.0);
        coupon.setUsageLimit(1);
        coupon.setTotalUsed(0);
        coupon.setValidFrom(Instant.now().minusSeconds(3600));
        coupon.setValidUntil(Instant.now().plusSeconds(3600));
    }

    @Test
    void reserveCouponUsage_whenAtLimit_throwsCouponLimitExceededException() {
        when(couponRepositoryCustom.incrementUsageIfBelowLimit("coupon-1")).thenReturn(Optional.empty());

        assertThrows(CouponLimitExceededException.class, () -> couponService.reserveCouponUsage("coupon-1"));
    }

    @Test
    void reserveCouponUsage_whenBelowLimit_succeeds() {
        Coupon updated = new Coupon();
        updated.setId("coupon-1");
        updated.setTotalUsed(1);
        when(couponRepositoryCustom.incrementUsageIfBelowLimit("coupon-1")).thenReturn(Optional.of(updated));

        assertDoesNotThrow(() -> couponService.reserveCouponUsage("coupon-1"));
        verify(couponRepositoryCustom).incrementUsageIfBelowLimit("coupon-1");
    }

    @Test
    void releaseCouponUsage_decrementsWhenAboveZero() {
        Coupon updated = new Coupon();
        updated.setId("coupon-1");
        updated.setTotalUsed(0);
        when(couponRepositoryCustom.decrementUsageIfAboveZero("coupon-1")).thenReturn(Optional.of(updated));

        couponService.releaseCouponUsage("coupon-1");

        verify(couponRepositoryCustom).decrementUsageIfAboveZero("coupon-1");
    }

    @Test
    void releaseCouponUsage_skipsBlankCouponId() {
        couponService.releaseCouponUsage("  ");

        verify(couponRepositoryCustom, never()).decrementUsageIfAboveZero(anyString());
    }

    @Test
    void validateCoupon_treatsNullTotalUsedAsZero() {
        coupon.setTotalUsed(null);
        coupon.setUsageLimit(1);
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        ValidateCouponRequestDTO dto = new ValidateCouponRequestDTO();
        dto.setCouponCode("ASAT60");
        dto.setAmount(100.0);

        assertDoesNotThrow(() -> couponService.validateCoupon(dto));
    }

    @Test
    void validateCoupon_rejectsWhenTotalUsedEqualsUsageLimit() {
        coupon.setTotalUsed(1);
        coupon.setUsageLimit(1);
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        ValidateCouponRequestDTO dto = new ValidateCouponRequestDTO();
        dto.setCouponCode("ASAT60");
        dto.setAmount(100.0);

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.validateCoupon(dto));
        assertEquals("Coupon usage limit has been reached.", ex.getMessage());
    }

    @Test
    void getCouponByCode_validCoupon_returnsDto() {
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        CouponCreateResponseDTO result = couponService.getCouponByCode("ASAT60");

        assertNotNull(result);
        assertEquals("ASAT60", result.getCode());
    }

    @Test
    void getCouponByCode_blankCode_throwsCouponValidationException() {
        assertThrows(CouponValidationException.class,
                () -> couponService.getCouponByCode("  "));
    }

    @Test
    void getCouponByCode_inactive_throwsCouponValidationException() {
        coupon.setActive(false);
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        assertThrows(CouponValidationException.class, () -> couponService.getCouponByCode("ASAT60"));
    }

    @Test
    void getCouponByCode_notYetValid_throwsCouponValidationException() {
        coupon.setValidFrom(Instant.now().plusSeconds(3600));
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        assertThrows(CouponValidationException.class, () -> couponService.getCouponByCode("ASAT60"));
    }

    @Test
    void getCouponByCode_expired_throwsCouponValidationException() {
        coupon.setValidUntil(Instant.now().minusSeconds(3600));
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        assertThrows(CouponValidationException.class, () -> couponService.getCouponByCode("ASAT60"));
    }

    @Test
    void getCouponByCode_usageLimitReached_throwsCouponValidationException() {
        coupon.setTotalUsed(1);
        coupon.setUsageLimit(1);
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.getCouponByCode("ASAT60"));
        assertEquals("Coupon usage limit has been reached.", ex.getMessage());
    }

    @Test
    void findCouponByCode_expiredCoupon_succeeds() {
        coupon.setValidUntil(Instant.now().minusSeconds(3600));
        when(couponRepository.findByCode("ASAT60")).thenReturn(Optional.of(coupon));

        CouponCreateResponseDTO result = couponService.findCouponByCode("ASAT60");

        assertEquals("ASAT60", result.getCode());
    }

    @Test
    void findCouponByCode_missingCode_throwsCouponNotFoundException() {
        when(couponRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.findCouponByCode("MISSING"));
    }

    @Test
    void getCoupons_noMatches_returnsEmptyList() {
        when(mongoTemplate.find(any(Query.class), eq(Coupon.class))).thenReturn(List.of());

        List<CouponCreateResponseDTO> result = couponService.getCoupons(
                0, 10, null, false, null, null, null, "ASC");

        assertTrue(result.isEmpty());
    }

    @Test
    void getCouponsCount_noMatches_returnsZero() {
        when(mongoTemplate.count(any(Query.class), eq(Coupon.class))).thenReturn(0L);

        long count = couponService.getCouponsCount(null, false, null, null, null);

        assertEquals(0L, count);
    }

    @Test
    void getCoupons_withOrderAsc_appliesCreatedAtAscSort() {
        when(mongoTemplate.find(any(Query.class), eq(Coupon.class))).thenReturn(List.of());

        couponService.getCoupons(0, 10, null, null, null, null, null, "ASC");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Coupon.class));
        assertEquals(1, captor.getValue().getSortObject().getInteger("createdAt"));
    }

    @Test
    void getCoupons_defaultOrder_appliesCreatedAtAscSort() {
        when(mongoTemplate.find(any(Query.class), eq(Coupon.class))).thenReturn(List.of());

        couponService.getCoupons(0, 10, null, null, null, null, null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Coupon.class));
        assertEquals(1, captor.getValue().getSortObject().getInteger("createdAt"));
    }

    @Test
    void getCoupons_withOrderDesc_appliesCreatedAtDescSort() {
        when(mongoTemplate.find(any(Query.class), eq(Coupon.class))).thenReturn(List.of());

        couponService.getCoupons(0, 10, null, null, null, null, null, "DESC");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Coupon.class));
        assertEquals(-1, captor.getValue().getSortObject().getInteger("createdAt"));
    }

    @Test
    void getCoupons_isActiveFilter_appliedToQuery() {
        when(mongoTemplate.find(any(Query.class), eq(Coupon.class))).thenReturn(List.of());

        couponService.getCoupons(0, 10, null, false, null, null, null, "ASC");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Coupon.class));
        assertTrue(captor.getValue().getQueryObject().toJson().contains("isActive"));
    }

    @Test
    void getCoupons_invalidCouponType_throwsBadRequest() {
        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                couponService.getCoupons(0, 10, null, null, null, "INVALID", null, "ASC"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getCoupons_invalidOrder_throwsBadRequest() {
        BillingServiceException ex = assertThrows(BillingServiceException.class, () ->
                couponService.getCoupons(0, 10, null, null, null, null, null, "INVALID"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void calculateCouponDiscount_multiplePackageRestrictions_appliesToMatchingPackageOnly() {
        ProductRestrictionDTO silver = new ProductRestrictionDTO();
        silver.setProductId("6913c4c0-44bf-45f9-87d9-6313ef643acf");
        silver.setPackageId("1776336540544");

        ProductRestrictionDTO platinum = new ProductRestrictionDTO();
        platinum.setProductId("6913c4c0-44bf-45f9-87d9-6313ef643acf");
        platinum.setPackageId("1776337643293");

        CouponCreateResponseDTO couponDto = new CouponCreateResponseDTO();
        couponDto.setType("PERCENTAGE");
        couponDto.setValue(5.0);
        couponDto.setProductRestrictions(List.of(silver, platinum));
        couponDto.setGeographicRestrictions(List.of());

        ProductSelectionDto line = ProductSelectionDto.builder()
                .productId("6913c4c0-44bf-45f9-87d9-6313ef643acf")
                .packageId("1776337643293")
                .licenseCount(20)
                .pricePerLicense(45.0)
                .validityPeriod(1)
                .build();

        CouponDiscountResponseDTO result = couponService.calculateCouponDiscount(
                List.of(line), "8bb7315f-f628-4065-8a9b-76aa98fc49f4", couponDto);

        assertEquals(900.0, result.getActualAmount());
        assertEquals(45.0, result.getDiscountAmount());
        assertEquals(855.0, result.getSubtotal());
    }
}
