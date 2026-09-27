package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AudienceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for phishing license allocation against a campaign's productPackageId.
 * Reuses the same audience selection shape as Step 6.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateLicenceRequest {

    @NotNull(message = "Audience type is required")
    private AudienceType audienceType;

    @Builder.Default
    private List<String> departmentIds = new ArrayList<>();

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    private List<String> userIds = new ArrayList<>();

    /**
     * When false and selection exceeds remaining seats, nothing is inserted and
     * {@code requiresConfirmation} is returned. When true, inserts up to remaining seats.
     */
    @Builder.Default
    private boolean confirm = false;
}
