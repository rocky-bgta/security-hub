package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "support_tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    private String id;

    private String ticketId;

    private String title;

    private String clientId;

    private String userId;

    private String username;

    private String userType;

    private String parentTicketId;

    private String assignedTo;

    private TicketStatus status;

    private Priority priority;

    private String supportType;

    private String productId;

    private String productName;

    private String packageId;

    private String courseId;

    private String mspId;

    private String description;

    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    private Instant createdDate;

    private Instant updatedDate;

    private String createdBy;

    private String updatedBy;

    @Builder.Default
    private Boolean assignToSuperAdmin = false;

    private AssignCategory assignCategory;
}

