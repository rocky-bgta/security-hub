package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.controller.BrandingController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.branding.request.BrandingCreateRequestDTO;
import com.aspire.asat.registration.data.branding.request.BrandingUpdateRequestDTO;
import com.aspire.asat.registration.data.branding.response.BrandingResponseDTO;
import com.aspire.asat.registration.service.BrandingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BrandingControllerImpl implements BrandingController {

    private final BrandingService brandingService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<BrandingResponseDTO>> createBranding(BrandingCreateRequestDTO requestDTO) {
        log.info("Creating branding: {}", requestDTO.getCompanyName());
        BrandingResponseDTO response = brandingService.createBranding(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Branding created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BrandingResponseDTO>> getBranding() {
        log.info("Getting branding for company");
        BrandingResponseDTO branding = brandingService.getBranding();
        return ResponseEntity.ok(new ApiResponseDto<>("Branding retrieved successfully", 200, branding));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BrandingResponseDTO>> updateBranding(BrandingUpdateRequestDTO requestDTO) {
        log.info("Updating branding: {}", requestDTO.getCompanyName());
        BrandingResponseDTO response = brandingService.updateBranding(requestDTO);
        String message = org.springframework.util.StringUtils.hasText(requestDTO.getLogoFilePath())
                ? messageService.get(MessageKeys.BRANDING_LOGO_UPDATED)
                : messageService.get(MessageKeys.BRANDING_SETTINGS_SAVED);
        return ResponseEntity.ok(new ApiResponseDto<>(message, 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteBranding(String brandingId) {
        log.info("Deleting branding: {}", brandingId);
        brandingService.deleteBranding(brandingId);
        return ResponseEntity.ok(new ApiResponseDto<>("Branding deleted successfully", 200, null));
    }
}
