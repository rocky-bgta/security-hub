package com.aspire.asat.registration.data.reports;

import com.aspire.asat.registration.data.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row in the User Details table of the User Summary Report. Mirrors the
 * columns shown in the admin UI: Name, Email, Department, Risk Group, Role,
 * Status, Last Login. {@code createdAt} is included for the CSV export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailRowDTO {

    private String name;
    private String email;
    private String department;
    private RiskGroup riskGroup;
    private String role;
    private String status;
    private Instant lastLoginAt;
    private Instant createdAt;
}
