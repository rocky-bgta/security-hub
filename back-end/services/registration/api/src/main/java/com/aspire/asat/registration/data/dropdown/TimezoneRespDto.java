package com.aspire.asat.registration.data.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimezoneRespDto {
    private String id;
    private String countryId;
    private String stateId;
    private String stateName;
    private String timezoneId;
    private String displayName;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
