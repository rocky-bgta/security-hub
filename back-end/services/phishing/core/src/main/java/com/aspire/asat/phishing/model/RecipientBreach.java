package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB entity for recipient breach records.
 * Tracks individual users affected by breaches.
 */
@Document(collection = "recipient_breaches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "client_breach_idx", def = "{'clientId': 1, 'breachRecordId': 1}"),
    @CompoundIndex(name = "client_user_idx", def = "{'clientId': 1, 'userId': 1}"),
    @CompoundIndex(name = "client_email_idx", def = "{'clientId': 1, 'email': 1}"),
    @CompoundIndex(name = "client_status_idx", def = "{'clientId': 1, 'status': 1}")
})
public class RecipientBreach {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private String breachRecordId;

    private String userId;

    @Indexed
    private String email;

    private String firstName;

    private String lastName;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private int breachCount = 1;

    @Builder.Default
    private RecipientBreachStatus status = RecipientBreachStatus.PENDING;

    private Instant notifiedAt;

    private Instant passwordResetAt;

    private Instant resolvedAt;

    @Builder.Default
    private List<ActionLog> actionLogs = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Add an action log entry
     */
    public void addActionLog(String action, String performedBy, String notes) {
        if (this.actionLogs == null) {
            this.actionLogs = new ArrayList<>();
        }
        this.actionLogs.add(ActionLog.builder()
                .action(action)
                .performedBy(performedBy)
                .performedAt(Instant.now())
                .notes(notes)
                .build());
    }

    /**
     * Get full name
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (lastName != null) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName);
        }
        return name.toString();
    }
}
