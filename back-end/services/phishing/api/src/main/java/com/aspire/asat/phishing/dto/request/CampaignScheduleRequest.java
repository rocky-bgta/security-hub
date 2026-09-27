package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Step 8: Schedule Configuration.
 * Date/time fields are strings (naive local); parsed using registration timezone display name resolved from {@code timezone} id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignScheduleRequest {

    @NotNull(message = "Schedule type is required")
    private ScheduleType scheduleType;

    private String startDateTime;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String endDateTime;

    private String timezone;

    @Builder.Default
    private SendingPattern sendingPattern = SendingPattern.ALL_AT_ONCE;

    private Integer batchSize;

    private Integer batchIntervalMinutes;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String recurringFrequency;

    private List<Integer> daysOfWeek;

    private Integer dayOfMonth;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String timeOfDay;

    private Integer repeatCount;

    private Boolean neverExpires;
}
