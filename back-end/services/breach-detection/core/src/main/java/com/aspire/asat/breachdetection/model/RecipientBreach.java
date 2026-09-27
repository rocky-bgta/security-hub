package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.RecipientBreachStatus;
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
}
