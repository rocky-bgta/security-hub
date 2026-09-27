package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;

import java.util.List;

public interface CouponService {
    CouponCreateResponseDTO createCoupon(CouponCreateRequestDTO dto);

    CouponQrResponseDTO generateCouponQrAndUpload(String couponCode);

    CouponCreateResponseDTO getCouponById(String id);

    List<CouponCreateResponseDTO> getCoupons(int offset, int limit, String searchParam, Boolean isActive,
            Boolean isExpired, String couponType, String productId, String order);

    long getCouponsCount(String searchParam, Boolean isActive, Boolean isExpired, String couponType, String productId);

    CouponCreateResponseDTO updateCoupon(String id, CouponCreateRequestDTO requestDTO);

    void deleteCoupon(String id);

    CouponCreateResponseDTO findCouponByCode(String code);

    CouponCreateResponseDTO getCouponByCode(String code);

    CouponCreateResponseDTO validateCoupon(ValidateCouponRequestDTO dto);

    /**
     * @deprecated Use calculateCouponDiscount(List, String, CouponCreateResponseDTO) instead.
     * This method is kept for backward compatibility but will throw UnsupportedOperationException.
     */
    @Deprecated
    CouponDiscountResponseDTO calculateCouponDiscount(PaymentRequestDTO paymentRequestDTO, CouponCreateResponseDTO coupon);

    /**
     * Calculate coupon discount using package items and client country ID directly.
     * 
     * @param packageItems List of product selections
     * @param clientCountryId Client country ID (UUID format)
     * @param coupon Coupon to apply
     * @return Calculated discount response
     */
    CouponDiscountResponseDTO calculateCouponDiscount(List<ProductSelectionDto> packageItems, String clientCountryId, CouponCreateResponseDTO coupon);

    /**
     * @deprecated Use calculateNormalBill(List) instead.
     * This method is kept for backward compatibility but will throw UnsupportedOperationException.
     */
    @Deprecated
    CouponDiscountResponseDTO calculateNormalBill(PaymentRequestDTO paymentRequestDTO);

    /**
     * Calculate normal bill without coupon using package items directly.
     * 
     * @param packageItems List of product selections
     * @return Calculated bill response
     */
    CouponDiscountResponseDTO calculateNormalBill(List<ProductSelectionDto> packageItems);

    /**
     * Validates a coupon for invoice redemption without mutating usage count.
     */
    CouponCreateResponseDTO validateForRedemption(String couponCode, double amount,
            List<ProductSelectionDto> packageItems, String clientCountryId);

    /**
     * Atomically reserves one usage slot. Throws CouponLimitExceededException if limit reached.
     */
    void reserveCouponUsage(String couponId);

    /**
     * Releases a previously reserved usage slot.
     */
    void releaseCouponUsage(String couponId);

    /**
     * Export coupons as CSV based on filter criteria
     * @param searchParam Search parameter for coupon code
     * @param isActive Filter by active status
     * @param isExpired Filter by expiration status
     * @param couponType Filter by coupon type (PERCENTAGE or FIXED)
     * @param productId Filter by product ID in product restrictions
     * @return CSV file as byte array
     */
    byte[] exportCouponsCsv(String searchParam, Boolean isActive, Boolean isExpired, String couponType, String productId);

    /**
     * Get coupon dashboard statistics
     * @return CouponDashboardResponseDTO with aggregated statistics
     */
    CouponDashboardResponseDTO getCouponDashboard();


}
