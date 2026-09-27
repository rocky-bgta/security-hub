package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.FileUploadController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.service.AzureStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FileUploadControllerImpl implements FileUploadController {

    private final AzureStorageService azureStorageService;

    public FileUploadControllerImpl(AzureStorageService azureStorageService) {
        this.azureStorageService = azureStorageService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getUploadUrl(@RequestParam String fileName) {
        String uploadUrl = azureStorageService.generateUploadUrl(fileName);
        String expiresAt = azureStorageService.extractExpiryTimeFromUrl(uploadUrl);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("uploadUrl", uploadUrl);
        responseData.put("fileName", fileName);
        responseData.put("expiresAt", expiresAt);
        responseData.put("method", "PUT");
        ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(
                "Upload link generated successfully",
                HttpStatus.OK.value(),
                responseData
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> checkUploadStatus(@RequestParam String fileName) {
        boolean exists = azureStorageService.isFileExists(fileName);
        Map<String, Object> responseData = new HashMap<>();
        if (exists) {

            String blobReferenceUrl = azureStorageService.getBlobReferenceUrl(fileName);
            responseData.put("message", "successfully uploaded");
            responseData.put("blobReference", blobReferenceUrl);
            ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(
                    "File exists in blob storage",
                    HttpStatus.OK.value(),
                    responseData
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            responseData.put("message", "upload failed");
            ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(
                    "File does not exist in blob storage",
                    HttpStatus.NOT_FOUND.value(),
                    responseData
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
