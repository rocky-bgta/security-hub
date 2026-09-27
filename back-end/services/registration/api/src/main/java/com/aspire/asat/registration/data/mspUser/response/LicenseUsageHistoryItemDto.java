package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.common.enums.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single license usage history entry (from activity log)")
public class LicenseUsageHistoryItemDto {

    @Schema(description = "Activity log ID")
    private String activityId;

    @Schema(description = "Action type", example = "LICENSE_ALLOCATED")
    private ActivityType actionType;

    @Schema(description = "Action description")
    private String description;

    @Schema(description = "Timestamp of the action")
    private Instant timestamp;

    @Schema(description = "Old value if applicable")
    private String oldValue;

    @Schema(description = "New value if applicable")
    private String newValue;
}
