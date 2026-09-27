package com.aspire.asat.universal.supportTicket.response;

import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for support ticket")
public class SupportTicketGetResponseDto {
    @Schema(description = "Unique ticket identifier", example = "ticket-123")
    private String id;

    @Schema(description = "Human-readable ticket ID in format TKT-yyyymmdd-XXX", example = "TKT-20241215-001")
    private String ticketId;

    @Schema(description = "Brief description or title for the issue", example = "Unable to access course materials")
    private String title;

    @Schema(description = "Client's ID", example = "client-123")
    private String clientId;

    @Schema(description = "User ID who created or is associated with the ticket", example = "user-456")
    private String userId;

    @Schema(description = "Username who created or is associated with the ticket", example = "john.doe")
    private String username;

    @Schema(description = "User type who created or is associated with the ticket", example = "END_USER")
    private String userType;

    @Schema(description = "Parent ticket ID if this is a follow-up or related ticket", example = "ticket-parent-789")
    private String parentTicketId;

    @Schema(description = "Support staff member assigned to the ticket", example = "support-user-456")
    private String assignedTo;

    @Schema(description = "Current status of the ticket", example = "OPEN")
    private TicketStatus status;

    @Schema(description = "Priority level of the ticket", example = "HIGH")
    private Priority priority;

    @Schema(description = "Type of support", example = "COURSE_ENROLLMENT_PROBLEM")
    private SupportTypeDto supportType;

    @Schema(description = "The product ID related to the issue", example = "product-789")
    private String productId;

    @Schema(description = "The product name related to the issue", example = "ASAT-SECURITY-V3")
    private String productName;

    @Schema(description = "The package ID related to the issue", example = "package-123")
    private String packageId;

    @Schema(description = "The course/subpackage ID related to the issue", example = "course-101")
    private String courseId;

    @Schema(description = "Managed Service Provider's ID, if applicable", example = "msp-202")
    private String mspId;

    @Schema(description = "Detailed description of the issue", example = "User is unable to access course materials after enrollment")
    private String description;

    @Schema(description = "List of attachment file paths or URLs")
    private List<String> attachments;

    @Schema(description = "Date and time when the ticket was created")
    private Instant createdDate;

    @Schema(description = "Date when the ticket was last updated")
    private Instant updatedDate;

    @Schema(description = "Person who created the ticket", example = "user-123")
    private String createdBy;

    @Schema(description = "Person who last updated the ticket", example = "user-123")
    private String updatedBy;

    @Schema(description = "Flag to indicate if ticket is assigned to super admin", example = "false")
    private Boolean assignToSuperAdmin;

    @Schema(description = "Assignment category of the ticket", example = "ASSIGNTOCLIENT")
    private AssignCategory assignCategory;
}