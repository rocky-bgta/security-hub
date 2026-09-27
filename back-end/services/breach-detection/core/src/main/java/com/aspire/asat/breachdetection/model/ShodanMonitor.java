package com.aspire.asat.breachdetection.model;

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

/** A single IP or domain registered for Shodan-backed breach monitoring. */
@Document(collection = "shodan_monitors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "client_subject_unique_idx",
                def = "{'clientId':1,'subjectType':1,'subject':1}", unique = true)
})
public class ShodanMonitor {

    @Id
    private String id;

    @Indexed
    private String clientId;

    private ShodanSubjectType subjectType;

    /** Canonical lower-cased value: IPv4/IPv6 or DNS name. */
    private String subject;

    private boolean enabled;
    private String notes;

    private Instant lastSyncAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
