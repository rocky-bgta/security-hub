package com.aspire.asat.universal.supportTicket.request;

import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.SupportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a support ticket")
public class SupportTicketCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(description = "Brief description or title for the issue", example = "Unable to access course materials", required = true)
    private String title;

    @Schema(description = "Parent ticket ID if this is a follow-up or related ticket", example = "ticket-parent-789")
    private String parentTicketId;

    @Schema(description = "Support staff member assigned to the ticket", example = "support-user-456")
    private String assignedTo;

    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level of the ticket", example = "HIGH", required = true)
    private Priority priority;

    @NotNull(message = "Support type is required")
    @Schema(description = "Type of support", example = "COURSE_ENROLLMENT_PROBLEM", required = true)
    private String supportType;

    @Schema(description = "The product ID related to the issue", example = "product-789")
    private String productId;

    @Schema(description = "The course/subpackage ID related to the issue", example = "course-101")
    private String courseId;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    @Schema(description = "Detailed description of the issue", example = "User is unable to access course materials after enrollment", required = true)
    private String description;

    @Schema(description = "List of attachment file paths or URLs")
    @Builder.Default
    private List<String> attachments = new ArrayList<>();
}
