package com.aspire.asat.registration.data.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row in the Subscription Detail list of the Subscription Summary Report.
 * Mirrors the columns shown in the admin UI: Client, Product, Package, Start /
 * End date, Status and license usage. The id fields are retained for downstream
 * navigation and are not part of the CSV export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDetailRowDTO {

    private String clientName;
    private String productName;
    private String packageName;
    private Instant startDate;
    private Instant endDate;
    private String status;
    private int totalLicense;
    private int usedLicense;
    private String clientAdminId;
    private String productId;
    private String packageId;
}
