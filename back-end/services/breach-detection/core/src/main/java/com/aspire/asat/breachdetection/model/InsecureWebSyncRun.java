package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "insecureweb_sync_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebSyncRun {
    @Id
    private String id;

    private String clientId;
    private Long organizationId;
    private Instant startedAt;
    private Instant completedAt;
    private SyncRunStatus status;
    private int pageStart;
    private int pageEnd;
    private int recordsFetched;
    private int recordsInserted;
    private int recordsUpdated;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;
}
