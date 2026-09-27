package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.BreachSeverity;
import com.aspire.asat.breachdetection.dto.enums.BreachStatus;
import jakarta.validation.constraints.NotBlank;
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

@Document(collection = "breach_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "client_domain_idx", def = "{'clientId': 1, 'domain': 1}"),
        @CompoundIndex(name = "client_status_idx", def = "{'clientId': 1, 'status': 1}"),
        @CompoundIndex(name = "client_severity_idx", def = "{'clientId': 1, 'severity': 1}")
})
public class BreachRecord {
    @Id
    private String id;

    @NotBlank
    @Indexed
    private String clientId;

    @NotBlank
    private String domain;

    private String breachName;
    private Instant dateOfBreach;
    private String description;

    @Builder.Default
    private List<String> compromisedDataTypes = new ArrayList<>();

    @Builder.Default
    private int recipientCount = 0;

    @Builder.Default
    private BreachSeverity severity = BreachSeverity.LOW;

    @Builder.Default
    private BreachStatus status = BreachStatus.ACTION_REQUIRED;

    private String sourceApi;

    @Indexed
    private String externalBreachId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public void calculateSeverity() {
        if (compromisedDataTypes == null || compromisedDataTypes.isEmpty()) {
            this.severity = BreachSeverity.LOW;
            return;
        }
        boolean hasPasswords = compromisedDataTypes.stream().anyMatch(type -> type.toLowerCase().contains("password"));
        boolean hasFinancial = compromisedDataTypes.stream().anyMatch(type ->
                type.toLowerCase().contains("credit") || type.toLowerCase().contains("bank") || type.toLowerCase().contains("ssn"));
        boolean hasPII = compromisedDataTypes.stream().anyMatch(type ->
                type.toLowerCase().contains("address") || type.toLowerCase().contains("phone") || type.toLowerCase().contains("dob"));

        if (hasPasswords || hasFinancial) {
            this.severity = BreachSeverity.HIGH;
        } else if (hasPII) {
            this.severity = BreachSeverity.MEDIUM;
        } else {
            this.severity = BreachSeverity.LOW;
        }
    }
}
