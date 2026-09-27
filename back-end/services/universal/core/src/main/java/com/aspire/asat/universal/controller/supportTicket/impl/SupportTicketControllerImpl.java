package com.aspire.asat.universal.controller.supportTicket.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentDto;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketCreateRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketUpdateRequestDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketGetResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketResponseDto;
import com.aspire.asat.universal.controller.supportTicket.SupportTicketController;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.service.SupportTicketCommentService;
import com.aspire.asat.universal.service.SupportTicketService;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class SupportTicketControllerImpl implements SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final SupportTicketCommentService commentService;
    private final UserCurrentContextService userCurrentContextService;
    private final MessageService messageService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created support ticket: #{#requestDto.title != null ? #requestDto.title : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> createSupportTicket(
            @org.springframework.web.bind.annotation.RequestBody SupportTicketCreateRequestDto requestDto) {
        SupportTicketResponseDto response = supportTicketService.createSupportTicket(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(messageService.get(MessageKeys.SUPPORT_TICKET_CREATED), 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketGetResponseDto>> getSupportTicketById(String id) {
        SupportTicketGetResponseDto response = supportTicketService.getSupportTicketById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket retrieved successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated support ticket: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDto.title != null ? #requestDto.title : #id}"
    )
    public ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> updateSupportTicket(
            String id, SupportTicketUpdateRequestDto requestDto) {
        SupportTicketResponseDto response = supportTicketService.updateSupportTicket(id, requestDto);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteSupportTicket(String id) {
        supportTicketService.deleteSupportTicket(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Support ticket deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketResponseDto>>>> getAllSupportTickets(
            String clientId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            int offset,
            int pageSize) {
        AllResponseDto<List<SupportTicketResponseDto>> response = supportTicketService.getAllSupportTickets(
                clientId, assignedTo, status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("Support tickets retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<OffsetPageDto<SupportTicketCommentDto>>> getCommentsForTicket(
            String id, int offset, int pageSize) {
        OffsetPageDto<SupportTicketCommentDto> response = commentService.getCommentsForTicketWithPagination(id, offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("Comments retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketCommentDto>> createComment(
            String id, SupportTicketCommentRequestDto requestDto) {
        SupportTicketCommentDto response = commentService.createComment(id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(messageService.get(MessageKeys.SUPPORT_COMMENT_ADDED), 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> updateTicketStatus(
            String id, TicketStatus status) {
        //get updatedBy from security context or session
        String updatedBy = userCurrentContextService.getCurrentUserContext().getUserId();
        SupportTicketResponseDto response = supportTicketService.updateTicketStatus(id, status, updatedBy);
        return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.SUPPORT_TICKET_STATUS_UPDATED), 200, response));
    }
}

