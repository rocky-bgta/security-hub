package com.aspire.asat.auth.controller.base;

import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.util.ApiResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeControllerImpl implements HomeController {

    @Override
    public ResponseEntity<ApiResponse<String>> index() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Welcome to Aspire Authentication Service")
                        .statusCode(ApiResponseCode.OPERATION_SUCCESSFUL.getResponseCode())
                        .data("This is the base page of the authentication service.")
                        .build()
        );
    }

    @Override
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Health Check Successful")
                        .statusCode(ApiResponseCode.OPERATION_SUCCESSFUL.getResponseCode())
                        .data("The Aspire Authentication Service is running smoothly.")
                        .build()
        );
    }
}
