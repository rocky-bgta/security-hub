package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Embedded model for campaign scheduling configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSchedule {

    @Builder.Default
    private ScheduleType type = ScheduleType.IMMEDIATELY;

    private Instant startDateTime;

    private Instant endDateTime;

    /** User-selected time zone label, e.g. EAT (UTC+03:00). */
    @JsonAlias("timezone")
    @Builder.Default
    private String timeZone = "UTC";

    private RecurringConfig recurring;

    private SendingConfig sendingConfig;
}
