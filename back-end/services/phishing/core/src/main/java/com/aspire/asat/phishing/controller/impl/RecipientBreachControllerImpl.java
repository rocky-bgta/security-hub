package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.RecipientBreachController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import com.aspire.asat.phishing.dto.request.RecipientActionRequest;
import com.aspire.asat.phishing.dto.response.RecipientBreachDto;
import com.aspire.asat.phishing.service.BreachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for recipient breach endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RecipientBreachControllerImpl implements RecipientBreachController {

    private final BreachService breachService;

    @Override
    public ResponseEntity<AllResponseDto<List<RecipientBreachDto>>> getRecipientBreaches(
            int offset, int pageSize, String breachRecordId,
            String keyword, RecipientBreachStatus status) {
        try {
            List<RecipientBreachDto> recipients = breachService.getRecipientBreaches(
                    offset, pageSize, breachRecordId, keyword, status);
            long totalCount = breachService.countRecipientBreaches(breachRecordId, keyword, status);

            return ResponseEntity.ok(AllResponseDto.<List<RecipientBreachDto>>builder()
                    .items(recipients)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting recipient breaches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<RecipientBreachDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<RecipientBreachDto>> getRecipientBreachById(String id) {
        try {
            RecipientBreachDto recipient = breachService.getRecipientBreachById(id);
            return ResponseEntity.ok(ApiResponseDto.<RecipientBreachDto>builder()
                    .data(recipient)
                    .message("Recipient breach retrieved successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<RecipientBreachDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<RecipientBreachDto>> notifyRecipient(
            String id, RecipientActionRequest request) {
        try {
            if (request == null) {
                request = RecipientActionRequest.builder().sendEmail(true).build();
            }
            RecipientBreachDto recipient = breachService.notifyRecipient(id, request);
            return ResponseEntity.ok(ApiResponseDto.<RecipientBreachDto>builder()
                    .data(recipient)
                    .message("User notified successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<RecipientBreachDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<RecipientBreachDto>> resetPassword(
            String id, RecipientActionRequest request) {
        try {
            if (request == null) {
                request = RecipientActionRequest.builder().build();
            }
            RecipientBreachDto recipient = breachService.resetRecipientPassword(id, request);
            return ResponseEntity.ok(ApiResponseDto.<RecipientBreachDto>builder()
                    .data(recipient)
                    .message("Password reset triggered successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<RecipientBreachDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<RecipientBreachDto>> resolveRecipientBreach(
            String id, RecipientActionRequest request) {
        try {
            if (request == null) {
                request = RecipientActionRequest.builder().build();
            }
            RecipientBreachDto recipient = breachService.resolveRecipientBreach(id, request);
            return ResponseEntity.ok(ApiResponseDto.<RecipientBreachDto>builder()
                    .data(recipient)
                    .message("Recipient breach resolved successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<RecipientBreachDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }
}
