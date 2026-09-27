package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.MspClientLicenseController;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseSummaryDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseUsageResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspClientLicenseDetailResponseDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.msp.MspClientLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MspClientLicenseControllerImpl implements MspClientLicenseController {

    private final MspClientLicenseService mspClientLicenseService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientLicenseSummaryDto>>>> getClientLicenseList(
            String mspId, Integer offset, Integer pageSize, String search) {
        try {
            log.info("Getting client license list for MSP: {}, offset: {}, pageSize: {}, search: {}", mspId, offset, pageSize, search);
            AllResponseDto<List<ClientLicenseSummaryDto>> result = mspClientLicenseService.getClientLicenseList(mspId, offset, pageSize, search);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license list retrieved successfully", 200, result));
        } catch (RegistrationServiceException e) {
            log.warn("Client license list failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting client license list for MSP: {}", mspId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve client license list: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspClientLicenseDetailResponseDto>> getClientLicenseDetail(String mspId, String clientAdminId) {
        try {
            log.info("Getting client license detail for MSP: {}, clientAdminId: {}", mspId, clientAdminId);
            MspClientLicenseDetailResponseDto result = mspClientLicenseService.getClientLicenseDetail(mspId, clientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license details retrieved successfully", 200, result));
        } catch (RegistrationServiceException e) {
            log.warn("Client license detail failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting client license detail for MSP: {}, clientAdminId: {}", mspId, clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve client license details: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientLicenseUsageResponseDto>> getClientLicenseUsage(String mspId, String clientAdminId) {
        try {
            log.info("Getting client license usage for MSP: {}, clientAdminId: {}", mspId, clientAdminId);
            ClientLicenseUsageResponseDto result = mspClientLicenseService.getClientLicenseUsage(mspId, clientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license usage retrieved successfully", 200, result));
        } catch (RegistrationServiceException e) {
            log.warn("Client license usage failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting client license usage for MSP: {}, clientAdminId: {}", mspId, clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve client license usage: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> exportClientLicenses(String mspId, String format) {
        try {
            log.info("Exporting client licenses for MSP: {}, format: {}", mspId, format);
            byte[] data = mspClientLicenseService.exportClientLicenses(mspId, format);
            String normalizedFormat = (format != null && !format.isBlank()) ? format.trim().toLowerCase() : "csv";
            String contentType = "text/csv; charset=UTF-8";
            String extension = "csv";
            if ("xlsx".equals(normalizedFormat) || "excel".equals(normalizedFormat)) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = "xlsx";
            } else if ("pdf".equals(normalizedFormat)) {
                contentType = MediaType.APPLICATION_PDF_VALUE;
                extension = "pdf";
            }
            String filename = "client-licenses-" + System.currentTimeMillis() + "." + extension;
            org.springframework.core.io.Resource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (RegistrationServiceException e) {
            log.warn("Export client licenses failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error exporting client licenses for MSP: {}", mspId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
