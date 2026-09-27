package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_import_jobs")
public class UserImportJob {

    @Id
    private String id;

    @Indexed
    private String clientAdminId;

    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED

    private List<String> groupIds;
    private String importType; // ON_DEMAND, RECURRING

    private int totalUsersProcessed;
    private int usersCreated;
    private int usersUpdated;
    private int usersSkipped;
    private int usersFailed;

    private List<ImportError> errors;

    private Instant startedAt;
    private Instant completedAt;

    private String createdBy;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private String userEmail;
        private String errorMessage;
        private String errorCode;
    }

}

