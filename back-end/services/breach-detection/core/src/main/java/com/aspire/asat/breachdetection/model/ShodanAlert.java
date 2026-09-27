package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
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

/** Single row in the "IP / Domain Breach Alerts" table. */
@Document(collection = "shodan_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "client_external_unique_idx",
                def = "{'clientId':1,'externalAlertId':1}", unique = true),
        @CompoundIndex(name = "client_subject_date_idx",
                def = "{'clientId':1,'subjectType':1,'dateDetected':-1}"),
        @CompoundIndex(name = "client_status_idx",
                def = "{'clientId':1,'status':1,'dateDetected':-1}")
})
public class ShodanAlert {

    @Id
    private String id;

    @Indexed
    private String clientId;

    private ShodanSubjectType subjectType;
    private String subject;

    /** Suspicious artifact surfaced in the "Fake Domain" column (may equal subject). */
    private String fakeSubject;

    private ShodanAlertType alertType;
    private ShodanAlertSeverity severity;
    @Builder.Default
    private ShodanAlertStatus status = ShodanAlertStatus.OPEN;

    private Instant dateDetected;
    private Instant firstSeenAt;
    private Instant lastSeenAt;

    private String title;
    private String description;
    private String source;

    @Builder.Default
    private List<String> evidence = new ArrayList<>();

    /** Stable idempotency key across sync runs — e.g. subject + alertType + fingerprint. */
    private String externalAlertId;

    private String rawPayloadJson;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
