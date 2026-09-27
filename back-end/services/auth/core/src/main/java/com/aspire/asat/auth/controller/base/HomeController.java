package com.aspire.asat.auth.controller.base;


import com.aspire.asat.auth.constant.WebApiUrlConstants;
import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Home", description = "Endpoints for base page")
@RequestMapping(value = WebApiUrlConstants.AUTH_HOME_INDEX, produces = "application/json")
public interface HomeController {

    @GetMapping
    @Operation(summary = "Home Page", description = "Welcome to the Aspire Authentication Service")
    ResponseEntity<ApiResponse<String>> index();

    @Hidden
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check the health status of the Aspire Authentication Service")
    ResponseEntity<ApiResponse<String>> healthCheck();

}
