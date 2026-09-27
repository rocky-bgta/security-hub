package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.CouponController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.CouponCreateRequestDTO;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDashboardResponseDTO;
import com.aspire.asat.billing.dto.CouponQrResponseDTO;
import com.aspire.asat.billing.exception.CouponValidationException;
import com.aspire.asat.billing.service.CouponService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CouponControllerImpl implements CouponController {

    private final CouponService couponService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created coupon: #{#dto.code != null ? #dto.code : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> createCoupon(CouponCreateRequestDTO dto) {
        log.info("Creating coupon with code: {}", dto.getCode());
        CouponCreateResponseDTO result = couponService.createCoupon(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Coupon created successfully", 201, result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CouponQrResponseDTO>> generateQr(String code) {
        log.info("Generating QR code for coupon: {}", code);
        CouponQrResponseDTO response = couponService.generateCouponQrAndUpload(code);
        return ResponseEntity.ok(new ApiResponseDto<>("QR code generated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponById(String id) {
        log.info("Getting coupon by ID: {}", id);
        CouponCreateResponseDTO result = couponService.getCouponById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon retrieved successfully", 200, result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CouponCreateResponseDTO>>>> getCoupons(
            int offset, int limit, Integer pageSize, String searchParam, Boolean isActive, Boolean isExpired,
            String couponType, String productId, String order) {
        int effectiveLimit = pageSize != null ? pageSize : limit;
        log.info("Getting coupons with filters - offset: {}, limit: {}, pageSize: {}, searchParam: {}, isActive: {}, isExpired: {}, couponType: {}, productId: {}, order: {}",
                offset, limit, pageSize, searchParam, isActive, isExpired, couponType, productId, order);
        List<CouponCreateResponseDTO> result = couponService.getCoupons(offset, effectiveLimit, searchParam, isActive,
                isExpired, couponType, productId, order);
        long total = couponService.getCouponsCount(searchParam, isActive, isExpired, couponType, productId);
        AllResponseDto<List<CouponCreateResponseDTO>> response = new AllResponseDto<>(offset, effectiveLimit, total, result);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupons retrieved successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated coupon: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.code != null ? #requestDTO.code : #id}"
    )
    public ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> updateCoupon(String id, CouponCreateRequestDTO requestDTO) {
        log.info("Updating coupon: {}", id);
        CouponCreateResponseDTO updated = couponService.updateCoupon(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCoupon(String id) {
        log.info("Deleting coupon: {}", id);
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponByCodeMissing() {
        throw new CouponValidationException("Coupon code is required");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponByCode(String code) {
        log.info("Getting coupon by code: {}", code);
        CouponCreateResponseDTO coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon retrieved successfully", 200, coupon));
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> exportCouponsCsv(
            String searchParam, Boolean isActive, Boolean isExpired, String couponType, String productId) {
        log.info("Exporting coupons to CSV with filters - searchParam: {}, isActive: {}, isExpired: {}, couponType: {}, productId: {}", 
                searchParam, isActive, isExpired, couponType, productId);
        
        byte[] csvData = couponService.exportCouponsCsv(searchParam, isActive, isExpired, couponType, productId);
        
        org.springframework.core.io.ByteArrayResource resource = new ByteArrayResource(csvData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=coupon-list-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CouponDashboardResponseDTO>> getCouponDashboard() {
        log.info("Fetching coupon dashboard statistics");
        CouponDashboardResponseDTO dashboard = couponService.getCouponDashboard();
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon dashboard retrieved successfully", 200, dashboard));
    }

}
