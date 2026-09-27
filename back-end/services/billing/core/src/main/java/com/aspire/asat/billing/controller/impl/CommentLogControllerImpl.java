package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.billing.controller.CommentLogController;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.CommentLogResponseDTO;
import com.aspire.asat.billing.service.CommentLogService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CommentLogControllerImpl implements CommentLogController {

    private final CommentLogService commentLogService;
    private final MessageService messageService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created comment log for invoice: #{#requestDTO.invoiceId != null ? #requestDTO.invoiceId : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> createCommentLog(CommentLogRequestDTO requestDTO) {
        log.info("Creating comment log for invoice: {}", requestDTO.getInvoiceId());
        CommentLogResponseDTO response = commentLogService.createCommentLog(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(messageService.get(MessageKeys.SUPPORT_COMMENT_ADDED), 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> getCommentLogById(String id) {
        log.info("Getting comment log by ID: {}", id);
        CommentLogResponseDTO response = commentLogService.getCommentLogById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment log retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<CommentLogResponseDTO>>> getCommentLogsByInvoiceId(String invoiceId) {
        log.info("Getting comment logs for invoice: {}", invoiceId);
        List<CommentLogResponseDTO> response = commentLogService.getCommentLogsByInvoiceId(invoiceId);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment logs retrieved successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated comment log: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.comment != null ? #requestDTO.comment : #id}"
    )
    public ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> updateCommentLog(String id, CommentLogRequestDTO requestDTO) {
        log.info("Updating comment log with ID: {}", id);
        CommentLogResponseDTO response = commentLogService.updateCommentLog(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment log updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCommentLog(String id) {
        log.info("Deleting comment log with ID: {}", id);
        commentLogService.deleteCommentLog(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment log deleted successfully", 200, null));
    }
}

