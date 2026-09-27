package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ClientProductController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.ClientPackageSimpleResponse;
import com.aspire.asat.cms.dto.client.ClientProductSimpleResponse;
import com.aspire.asat.cms.service.ClientProductService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for client product operations
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientProductControllerImpl implements ClientProductController {

    private final ClientProductService clientProductService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<List<ClientProductSimpleResponse>>> getUniqueProductsByClientAdminId(String clientAdminId) {
        log.info("Received request to get unique products for clientAdminId: {}", clientAdminId);

        List<ClientProductSimpleResponse> products = clientProductService.getUniqueProductsByClientAdminId(clientAdminId);

        ApiResponseDto<List<ClientProductSimpleResponse>> response = new ApiResponseDto<>(
            "Products retrieved successfully",
            HttpStatus.OK.value(),
            products
        );

        log.info("Successfully retrieved {} products for clientAdminId: {}", products.size(), clientAdminId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ClientPackageSimpleResponse>>> getPackagesByClientAdminIdAndProductId(String productId) {
        // Get clientAdminId from user context
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String clientAdminId = userContext.getUserId();
        
        log.info("Received request to get packages for clientAdminId: {} (from context) and productId: {}", clientAdminId, productId);

        List<ClientPackageSimpleResponse> packages = clientProductService.getPackagesByClientAdminIdAndProductId(clientAdminId, productId);

        ApiResponseDto<List<ClientPackageSimpleResponse>> response = new ApiResponseDto<>(
            "Packages retrieved successfully",
            HttpStatus.OK.value(),
            packages
        );

        log.info("Successfully retrieved {} packages for clientAdminId: {} and productId: {}", packages.size(), clientAdminId, productId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

