package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "License Overview summary cards for a client admin")
public class LicenseOverviewSummaryDto {

    @Schema(description = "Sum of purchased seats on ACTIVE client products", example = "995")
    private int totalLicenses;

    @Schema(description = "Seats currently in use (Assigned Users card)", example = "850")
    private int assignedUsers;

    @Schema(description = "assignedUsers as a percent of totalLicenses", example = "85.4")
    private double assignedUsersPercent;

    @Schema(description = "Unassigned purchased seats", example = "145")
    private int availableSeats;

    @Schema(description = "availableSeats as a percent of totalLicenses", example = "14.6")
    private double availableSeatsPercent;

    @Schema(description = "Seat utilization rate (same basis as assignedUsersPercent)", example = "85.4")
    private double utilizationRate;

    @Schema(description = "utilizationRate minus utilization as of 30 days ago, in percentage points", example = "3.6")
    private double utilizationChangeVsLastMonth;

    @Schema(description = "ACTIVE product-package assignments expiring within expiringSoonDays", example = "12")
    private int expiringSoon;

    @Schema(description = "Window used for Expiring Soon, in days", example = "30")
    private int expiringSoonDays;

    @Schema(description = "Distinct products with ACTIVE licenses", example = "6")
    private int activeProducts;
}
