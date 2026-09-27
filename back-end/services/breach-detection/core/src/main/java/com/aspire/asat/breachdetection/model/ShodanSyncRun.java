package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "shodan_sync_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "client_subject_started_idx",
                def = "{'clientId':1,'subjectType':1,'startedAt':-1}")
})
public class ShodanSyncRun {

    @Id
    private String id;

    private String clientId;
    private ShodanSubjectType subjectType;

    private Instant startedAt;
    private Instant completedAt;

    private SyncRunStatus status;

    private int subjectsProcessed;
    private int alertsCreated;
    private int alertsUpdated;
    private int errorsEncountered;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;
}
