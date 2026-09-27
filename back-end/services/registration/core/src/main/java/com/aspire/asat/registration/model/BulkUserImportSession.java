package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bulk_user_import_sessions")
public class BulkUserImportSession {

    public static final String STATUS_VALIDATED = "VALIDATED";
    public static final String STATUS_ONBOARDING = "ONBOARDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    private String id;

    @Indexed
    private String clientAdminId;

    private String createdBy;

    private String status;

    @Builder.Default
    private List<BulkImportSessionRow> rows = new ArrayList<>();

    private Instant createdAt;

    /** Mongo TTL deletes the document when this time is reached (expireAfterSeconds = 0). */
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportSessionRow {
        private int rowIndex;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String phoneCode;
        private String countryCode;
        private String department;
        private boolean valid;
        private String failureReason;
    }
}
