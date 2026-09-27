package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.BreachController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import com.aspire.asat.phishing.dto.request.BreachStatusRequest;
import com.aspire.asat.phishing.dto.response.BreachRecordDto;
import com.aspire.asat.phishing.service.BreachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Controller implementation for breach record endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BreachControllerImpl implements BreachController {

    private final BreachService breachService;

    @Override
    public ResponseEntity<AllResponseDto<List<BreachRecordDto>>> getBreaches(
            int offset, int pageSize, String keyword, String domain,
            BreachStatus status, BreachSeverity severity,
            Instant startDate, Instant endDate) {
        try {
            List<BreachRecordDto> breaches = breachService.getBreaches(
                    offset, pageSize, keyword, domain, status, severity, startDate, endDate);
            long totalCount = breachService.countBreaches(
                    keyword, domain, status, severity, startDate, endDate);

            return ResponseEntity.ok(AllResponseDto.<List<BreachRecordDto>>builder()
                    .items(breaches)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting breaches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<BreachRecordDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachRecordDto>> getBreachById(String id) {
        try {
            BreachRecordDto breach = breachService.getBreachById(id);
            return ResponseEntity.ok(ApiResponseDto.<BreachRecordDto>builder()
                    .data(breach)
                    .message("Breach retrieved successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<BreachRecordDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachRecordDto>> updateBreachStatus(
            String id, BreachStatusRequest request) {
        try {
            BreachRecordDto breach = breachService.updateBreachStatus(id, request);
            return ResponseEntity.ok(ApiResponseDto.<BreachRecordDto>builder()
                    .data(breach)
                    .message("Breach status updated successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<BreachRecordDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteBreach(String id) {
        try {
            breachService.deleteBreach(id);
            return ResponseEntity.ok(ApiResponseDto.<String>builder()
                    .data(id)
                    .message("Breach deleted successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<String>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportBreaches(String format, String domain, BreachStatus status) {
        try {
            byte[] data = breachService.exportBreaches(format, domain, status);
            
            String contentType = "text/csv";
            String filename = "breaches-export.csv";
            
            if ("excel".equalsIgnoreCase(format)) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = "breaches-export.xlsx";
            } else if ("pdf".equalsIgnoreCase(format)) {
                contentType = "application/pdf";
                filename = "breaches-export.pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting breaches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
