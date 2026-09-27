package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for campaign schedule data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignScheduleDto {

    private ScheduleType type;
    private Instant startDateTime;
    private Instant endDateTime;
    /** Display label from UI, e.g. {@code EAT (UTC+03:00)}. */
    @JsonAlias("timezone")
    private String timeZone;
    private SendingPattern sendingPattern;
    private Integer batchSize;
    private Integer batchIntervalMinutes;
    private String recurringFrequency;
    private String recurringDescription;
}
