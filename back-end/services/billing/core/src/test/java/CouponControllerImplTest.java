import com.aspire.asat.billing.controller.impl.CouponControllerImpl;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.CouponCreateRequestDTO;
import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.exception.CouponValidationException;
import com.aspire.asat.billing.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CouponControllerImplTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponControllerImpl couponController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCoupon_ShouldReturnCreatedCoupon() {
        // Arrange
        CouponCreateRequestDTO request = new CouponCreateRequestDTO();
        request.setCode("SPRING2025");

        CouponCreateResponseDTO responseDto = new CouponCreateResponseDTO();
        responseDto.setCode("SPRING2025");

        when(couponService.createCoupon(request)).thenReturn(responseDto);

        // Act
        ResponseEntity<ApiResponseDto<CouponCreateResponseDTO>> response = couponController.createCoupon(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatusCode());
        assertEquals("Coupon created successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("SPRING2025", response.getBody().getData().getCode());

        verify(couponService, times(1)).createCoupon(request);
    }

    @Test
    void getCoupons_pageSizeAlias_usedInsteadOfLimit() {
        when(couponService.getCoupons(eq(0), eq(25), any(), any(), any(), any(), any(), eq("ASC")))
                .thenReturn(List.of());
        when(couponService.getCouponsCount(any(), any(), any(), any(), any())).thenReturn(0L);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<CouponCreateResponseDTO>>>> response =
                couponController.getCoupons(0, 10, 25, null, null, null, null, null, "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getData().getTotal());
        assertTrue(response.getBody().getData().getItems().isEmpty());
        assertEquals(25, response.getBody().getData().getPageSize());
        verify(couponService).getCoupons(0, 25, null, null, null, null, null, "ASC");
    }

    @Test
    void getCouponByCodeMissing_throwsCouponValidationException() {
        assertThrows(CouponValidationException.class,
                () -> couponController.getCouponByCodeMissing());
    }
}
