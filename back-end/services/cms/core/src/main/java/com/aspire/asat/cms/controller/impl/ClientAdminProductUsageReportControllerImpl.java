package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ClientAdminProductUsageReportController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientAdminProductUsageReport.ClientAdminProductUsageReportResponseDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.service.clientAdminProductUsageReport.ClientAdminProductUsageReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClientAdminProductUsageReportControllerImpl implements ClientAdminProductUsageReportController {

    private final ClientAdminProductUsageReportService clientAdminProductUsageReportService;

    @Override
    public ResponseEntity<ApiResponseDto<ClientAdminProductUsageReportResponseDto>> getClientAdminProductUsageReport(
            String clientAdminId) {
        try {
            ClientAdminProductUsageReportResponseDto response =
                    clientAdminProductUsageReportService.getClientAdminProductUsageReport(clientAdminId);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Client admin product usage report retrieved successfully", 200, response));
        } catch (ResourceNotFoundException e) {
            log.error("Client admin product usage report not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid client admin product usage report request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving client admin product usage report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve client admin product usage report: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<Resource> exportClientAdminProductUsageReportCsv(String clientAdminId) {
        try {
            byte[] csv = clientAdminProductUsageReportService.exportClientAdminProductUsageReportCsv(clientAdminId);
            String fileName = "client-admin-product-usage-report-" + Instant.now().toEpochMilli() + ".csv";
            ByteArrayResource resource = new ByteArrayResource(csv);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(resource);
        } catch (ResourceNotFoundException e) {
            log.warn("Client admin product usage report CSV not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid client admin product usage report CSV export request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error exporting client admin product usage report CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
