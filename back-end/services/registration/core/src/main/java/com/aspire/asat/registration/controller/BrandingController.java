package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.branding.request.BrandingCreateRequestDTO;
import com.aspire.asat.registration.data.branding.request.BrandingUpdateRequestDTO;
import com.aspire.asat.registration.data.branding.response.BrandingResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Branding Management", description = "APIs for managing branding (create, list, update, delete)")
@RequestMapping(value = WebApiUrlConstants.BRANDING, produces = "application/json")
public interface BrandingController {

    @PostMapping()
    ResponseEntity<ApiResponseDto<BrandingResponseDTO>> createBranding(
            @RequestBody BrandingCreateRequestDTO requestDTO);

    @GetMapping()
    ResponseEntity<ApiResponseDto<BrandingResponseDTO>> getBranding();

    @PutMapping()
    ResponseEntity<ApiResponseDto<BrandingResponseDTO>> updateBranding(
            @RequestBody BrandingUpdateRequestDTO requestDTO);

    @DeleteMapping("/{brandingId}")
    ResponseEntity<ApiResponseDto<Void>> deleteBranding(
            @PathVariable String brandingId);
}

