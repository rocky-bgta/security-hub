package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import com.aspire.asat.phishing.dto.request.BreachStatusRequest;
import com.aspire.asat.phishing.dto.response.BreachRecordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Controller interface for breach record endpoints.
 */
@Tag(name = "Breaches", description = "APIs for managing data breach records")
@RequestMapping(value = WebApiUrlConstants.BREACHES_PATH)
public interface BreachController {

    @Operation(summary = "List data breaches")
    @GetMapping
    ResponseEntity<AllResponseDto<List<BreachRecordDto>>> getBreaches(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) BreachStatus status,
            @RequestParam(required = false) BreachSeverity severity,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate
    );

    @Operation(summary = "Get breach details")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<BreachRecordDto>> getBreachById(
            @PathVariable String id
    );

    @Operation(summary = "Update breach status")
    @PutMapping("/{id}/status")
    ResponseEntity<ApiResponseDto<BreachRecordDto>> updateBreachStatus(
            @PathVariable String id,
            @RequestBody BreachStatusRequest request
    );

    @Operation(summary = "Delete breach record")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> deleteBreach(
            @PathVariable String id
    );

    @Operation(summary = "Export breaches to PDF/Excel/CSV")
    @GetMapping("/export")
    ResponseEntity<byte[]> exportBreaches(
            @Parameter(description = "Export format: pdf, excel, csv")
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) BreachStatus status
    );
}
