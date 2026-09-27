package com.aspire.asat.universal.controller.supportTicket.impl;

import com.aspire.asat.universal.controller.supportTicket.SupportResolutionTimeController;
import com.aspire.asat.universal.service.SupportResolutionTimeService;
import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SupportResolutionTimeControllerImpl implements SupportResolutionTimeController {

    private final SupportResolutionTimeService supportResolutionTimeService;

    @Override
    public ResponseEntity<ApiResponseDto<SupportResolutionTimeResponseDto>> getSupportResolutionTime(
            String clientAdminId) {
        try {
            SupportResolutionTimeResponseDto response =
                    supportResolutionTimeService.getSupportResolutionTime(clientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Support resolution time metrics fetched successfully", 200, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get support resolution time metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to get support resolution time metrics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public void exportSupportResolutionTimeCsv(String clientAdminId, HttpServletResponse response) {
        try {
            byte[] csvBytes = supportResolutionTimeService.exportSupportResolutionTimeCsv(clientAdminId);
            response.setContentType("text/csv");
            String fileName = "Support_Resolution_Time_" + Instant.now().toEpochMilli() + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength(csvBytes.length);
            response.getOutputStream().write(csvBytes);
            response.getOutputStream().flush();
        } catch (IllegalArgumentException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        } catch (Exception e) {
            log.error("Failed to export support resolution time CSV", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to export support resolution time CSV: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }
}
