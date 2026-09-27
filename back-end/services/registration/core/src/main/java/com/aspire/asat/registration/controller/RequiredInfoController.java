package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.requiredinfo.RequiredInfoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.aspire.asat.registration.constant.WebApiUrlConstants.REQUIRED_INFO_API;

/**
 * Controller interface for required info endpoints
 */
@Tag(name = "Required Info", description = "Endpoints for checking required information status")
@RequestMapping(value = REQUIRED_INFO_API, produces = "application/json")
public interface RequiredInfoController {

    @GetMapping
    @Operation(summary = "Get Required Info", 
               description = "Get status of branding, user creation, and product assignment for the logged-in user")
    ResponseEntity<ApiResponseDto<RequiredInfoResponseDTO>> getRequiredInfo();
}

