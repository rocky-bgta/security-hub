package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.EmailTypeController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.EmailTypeCreateRequest;
import com.aspire.asat.phishing.dto.response.EmailTypeDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.EmailTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for EmailType configuration.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailTypeControllerImpl implements EmailTypeController {

    private final EmailTypeService emailTypeService;

    @Override
    public ResponseEntity<AllResponseDto<List<EmailTypeDto>>> getEmailTypes(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<EmailTypeDto> items = emailTypeService.getEmailTypes(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = emailTypeService.countEmailTypes(searchParam, isActive);

            return ResponseEntity.ok(AllResponseDto.<List<EmailTypeDto>>builder()
                    .items(items)
                    .total(total)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting email types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<EmailTypeDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .offset(offset)
                            .pageSize(pageSize)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailTypeDto>> getEmailTypeById(String id) {
        try {
            EmailTypeDto dto = emailTypeService.getEmailTypeById(id);
            return ResponseEntity.ok(ApiResponseDto.<EmailTypeDto>builder()
                    .data(dto)
                    .message("Email type retrieved successfully")
                    .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<EmailTypeDto>builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error getting email type by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<EmailTypeDto>builder()
                            .message("Failed to retrieve email type")
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailTypeDto>> createEmailType(EmailTypeCreateRequest request) {
        try {
            EmailTypeDto created = emailTypeService.createEmailType(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.<EmailTypeDto>builder()
                            .data(created)
                            .message("Email type created successfully")
                            .build());
        } catch (Exception e) {
            log.error("Error creating email type", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.<EmailTypeDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }
}

