package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Read-only view of the registration {@code timezones} collection for resolving
 * {@code client_admins.timeZone} document ids into offset labels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "timezones")
public class Timezone {

    @Id
    private String id;
    private String countryId;
    private String stateId;
    private String timezoneId;
    private String displayName;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
