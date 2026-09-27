package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import com.aspire.asat.phishing.dto.request.RecipientActionRequest;
import com.aspire.asat.phishing.dto.response.RecipientBreachDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller interface for recipient breach endpoints.
 */
@Tag(name = "Recipient Breaches", description = "APIs for managing recipient breaches")
@RequestMapping(value = WebApiUrlConstants.RECIPIENT_BREACHES_PATH)
public interface RecipientBreachController {

    @Operation(summary = "List recipient breaches")
    @GetMapping
    ResponseEntity<AllResponseDto<List<RecipientBreachDto>>> getRecipientBreaches(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String breachRecordId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecipientBreachStatus status
    );

    @Operation(summary = "Get recipient breach details")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<RecipientBreachDto>> getRecipientBreachById(
            @PathVariable String id
    );

    @Operation(summary = "Notify user about breach")
    @PostMapping("/{id}/notify")
    ResponseEntity<ApiResponseDto<RecipientBreachDto>> notifyRecipient(
            @PathVariable String id,
            @RequestBody(required = false) RecipientActionRequest request
    );

    @Operation(summary = "Trigger password reset for user")
    @PostMapping("/{id}/reset-password")
    ResponseEntity<ApiResponseDto<RecipientBreachDto>> resetPassword(
            @PathVariable String id,
            @RequestBody(required = false) RecipientActionRequest request
    );

    @Operation(summary = "Mark breach as resolved for user")
    @PutMapping("/{id}/resolve")
    ResponseEntity<ApiResponseDto<RecipientBreachDto>> resolveRecipientBreach(
            @PathVariable String id,
            @RequestBody(required = false) RecipientActionRequest request
    );
}
