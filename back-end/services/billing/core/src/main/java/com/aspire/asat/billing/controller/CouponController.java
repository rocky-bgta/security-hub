package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.CouponCreateRequestDTO;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.CouponDashboardResponseDTO;
import com.aspire.asat.billing.dto.CouponQrResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping(value = WebApiUrlConstants.COUPON_API, produces = "application/json")
@Tag(name = "Coupon Management", description = "Endpoints for Coupon Management")
public interface CouponController {

    @Operation(
            summary = "Create a new coupon",
            description = "Allows admins to create a new discount coupon with fixed or percentage-based value, product and geo restrictions, and usage limits."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request: Invalid data or validation failed (e.g. date range, currency)"),
            @ApiResponse(responseCode = "409", description = "Conflict: Coupon code already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity: Unsupported coupon type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(WebApiUrlConstants.CREATE)
    ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> createCoupon(@NotNull @Valid @RequestBody CouponCreateRequestDTO couponCreateRequestDTO);


    @Operation(
            summary = "Generate QR code for a coupon",
            description = "Creates and uploads a QR code that links to the coupon URL. Requires the coupon to exist and be active."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR code generated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Coupon is inactive"),
            @ApiResponse(responseCode = "404", description = "Not Found: Coupon not found"),
            @ApiResponse(responseCode = "410", description = "Gone: Coupon has expired"),
            @ApiResponse(responseCode = "424", description = "Failed Dependency: QR upload to Azure failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{code}/qr")
    ResponseEntity<ApiResponseDto<CouponQrResponseDTO>> generateQr(@PathVariable("code") @NotBlank(message = "Coupon code must not be blank") String code);


    @Operation(summary = "Get coupon by ID", description = "Fetches coupon details by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coupon ID"),
            @ApiResponse(responseCode = "404", description = "Coupon not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/id/{id}")
    ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponById(@PathVariable("id") @NotBlank String id);


    @Operation(summary = "List coupons", description = "Returns a paginated list of coupons. Optionally filter by coupon code, type, product ID, active status, and expiration. Results are sorted by createdAt (default ASC). Returns empty items when no coupons match.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupons listed successfully (may include empty items when no matches)"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters, sort order, coupon type, or product ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CouponCreateResponseDTO>>>> getCoupons(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Offset must be at least 0") int offset,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be at least 1") int limit,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String searchParam,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isExpired,
            @RequestParam(required = false) String couponType,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "ASC") String order);


    @Operation(summary = "Update an existing coupon", description = "Updates coupon details by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request: Invalid coupon data or ID"),
            @ApiResponse(responseCode = "404", description = "Coupon not found with the specified ID"),
            @ApiResponse(responseCode = "409", description = "Conflict: Coupon code already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity: Unsupported coupon type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{id}")
    ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> updateCoupon(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody CouponCreateRequestDTO requestDTO);


    @Operation(summary = "Delete a coupon", description = "Deletes a coupon by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coupon ID"),
            @ApiResponse(responseCode = "404", description = "Coupon not found with the specified ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteCoupon(
            @PathVariable("id") @NotBlank String id);

    @Operation(summary = "Get coupon by code", description = "Fetches coupon details by its unique code after validating availability (active, validity window, usage limit).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Coupon validation failed (inactive, expired, not yet valid, usage limit reached, or missing code)"),
            @ApiResponse(responseCode = "404", description = "Coupon not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping({"/code", "/code/"})
    ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponByCodeMissing();

    @GetMapping("/code/{code}")
    ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> getCouponByCode(@PathVariable("code") @NotBlank String code);

    @Operation(summary = "Export coupon list as CSV", description = "Generates a CSV file of all filtered coupons and returns it as a download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "404", description = "No coupons found matching criteria"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/export/csv")
    ResponseEntity<org.springframework.core.io.Resource> exportCouponsCsv(
            @RequestParam(required = false) String searchParam,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isExpired,
            @RequestParam(required = false) String couponType,
            @RequestParam(required = false) String productId);

    @Operation(summary = "Get coupon dashboard statistics", description = "Returns aggregated statistics about coupons including counts, usage, and estimated savings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    ResponseEntity<ApiResponseDto<CouponDashboardResponseDTO>> getCouponDashboard();



}
