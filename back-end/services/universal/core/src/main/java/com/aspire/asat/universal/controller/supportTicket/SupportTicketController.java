package com.aspire.asat.universal.controller.supportTicket;


import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentDto;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketCreateRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketUpdateRequestDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketGetResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketResponseDto;
import com.aspire.asat.universal.constant.WebApiUrlConstants;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Support Ticket Management", description = "APIs for managing support tickets (create, read, update, delete)")
@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT + "/support-tickets", produces = "application/json")
public interface SupportTicketController {

    @Operation(
            summary = "Create a new support ticket",
            description = "Creates a new support ticket with the provided details. The ticket will be created with OPEN status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Support ticket created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> createSupportTicket(
            @Parameter(description = "Request body containing support ticket details", required = true)
            @Valid @RequestBody SupportTicketCreateRequestDto requestDto
    );

    @Operation(
            summary = "Get support ticket by ID",
            description = "Retrieves a specific support ticket by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Support ticket retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ticket ID")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<SupportTicketGetResponseDto>> getSupportTicketById(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id
    );

    @Operation(
            summary = "Update support ticket",
            description = "Updates an existing support ticket. Only provided fields will be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Support ticket updated successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> updateSupportTicket(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id,
            @Parameter(description = "Request body containing updated support ticket details", required = true)
            @Valid @RequestBody SupportTicketUpdateRequestDto requestDto
    );

    @Operation(
            summary = "Delete support ticket",
            description = "Deletes a support ticket by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Support ticket deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ticket ID")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteSupportTicket(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id
    );

    @Operation(
            summary = "Get all support tickets",
            description = "Retrieves a paginated list of support tickets with optional filters for client, assigned staff, status, priority, support type, MSP, createdBy, assignCategory, and search term."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Support tickets retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketResponseDto>>>> getAllSupportTickets(
            @Parameter(description = "Filter by client ID")
            @RequestParam(required = false) String clientId,
            @Parameter(description = "Filter by assigned staff member")
            @RequestParam(required = false) String assignedTo,
            @Parameter(description = "Filter by ticket status")
            @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Filter by priority level")
            @RequestParam(required = false) Priority priority,
            @Parameter(description = "Filter by support type")
            @RequestParam(required = false) String supportType,
            @Parameter(description = "Filter by MSP ID")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Filter by assignToSuperAdmin flag (true/false)")
            @RequestParam(required = false) Boolean assignToSuperAdmin,
            @Parameter(description = "Filter by createdBy (user ID who created the ticket)")
            @RequestParam(required = false) String createdBy,
            @Parameter(description = "Filter by assignCategory (ASSIGNTOCLIENT, ASSIGNTOMSP, ASSIGNTOSUPER)")
            @RequestParam(required = false) AssignCategory assignCategory,
            @Parameter(description = "Search term for title or description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page offset for pagination", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Page size for pagination", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int pageSize
    );

    @Operation(
            summary = "Get all comments for a support ticket",
            description = "Retrieves paginated comments for a support ticket with threaded replies. Pagination is applied to root comments only; all replies are included for each root comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket not found")
    })
    @GetMapping("/{id}/comments")
    ResponseEntity<ApiResponseDto<OffsetPageDto<SupportTicketCommentDto>>> getCommentsForTicket(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id,
            @Parameter(description = "Page offset for pagination (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Page size for pagination", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int pageSize
    );

    @Operation(
            summary = "Add comment to support ticket",
            description = "Adds a comment to an existing support ticket. If parentCommentId is provided, creates a reply to that comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket or parent comment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/{id}/comments")
    ResponseEntity<ApiResponseDto<SupportTicketCommentDto>> createComment(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id,
            @Parameter(description = "Request body containing comment details", required = true)
            @Valid @RequestBody SupportTicketCommentRequestDto requestDto
    );

    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment by its ID. All replies to this comment will also be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @Parameter(description = "Unique identifier of the comment", required = true)
            @PathVariable("commentId") @NotBlank String commentId
    );

    @Operation(
            summary = "Update ticket status",
            description = "Updates the status of a support ticket (OPEN, IN_PROGRESS, CLOSED)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Support ticket not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}/status")
    ResponseEntity<ApiResponseDto<SupportTicketResponseDto>> updateTicketStatus(
            @Parameter(description = "Unique identifier of the support ticket", required = true)
            @PathVariable("id") @NotBlank String id,
            @Parameter(description = "New status for the ticket", required = true)
            @RequestParam("status") @NotNull TicketStatus status
    );
}

