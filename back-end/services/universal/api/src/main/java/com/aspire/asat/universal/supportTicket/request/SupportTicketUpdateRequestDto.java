package com.aspire.asat.universal.supportTicket.request;

import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.SupportType;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating a support ticket")
public class SupportTicketUpdateRequestDto {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(description = "Brief description or title for the issue", example = "Unable to access course materials")
    private String title;

    @Schema(description = "User ID who created or is associated with the ticket", example = "user-456")
    private String userId;

    @Schema(description = "Parent ticket ID if this is a follow-up or related ticket", example = "ticket-parent-789")
    private String parentTicketId;

    @Schema(description = "Support staff member assigned to the ticket", example = "support-user-456")
    private String assignedTo;

    @Schema(description = "Current status of the ticket", example = "IN_PROGRESS")
    private TicketStatus status;

    @Schema(description = "Priority level of the ticket", example = "HIGH")
    private Priority priority;

    @Schema(description = "Type of support", example = "COURSE_ENROLLMENT_PROBLEM")
    private String supportType;

    @Schema(description = "The product ID related to the issue", example = "product-789")
    private String productId;

    @Schema(description = "The course/subpackage ID related to the issue", example = "course-101")
    private String courseId;

    @Schema(description = "Managed Service Provider's ID, if applicable", example = "msp-202")
    private String mspId;

    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    @Schema(description = "Detailed description of the issue", example = "User is unable to access course materials after enrollment")
    private String description;

    @Schema(description = "List of attachment file paths or URLs")
    private List<String> attachments;

    @Schema(description = "Person who last updated the ticket", example = "user-123")
    private String updatedBy;
}

