package com.aspire.asat.registration.data.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One data point on the User Growth Trend chart. {@code totalUsers} is the
 * cumulative number of users that existed (createdAt <= end-of-month) at the
 * end of the indicated month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthTrendPointDTO {

    private int year;
    private int month;
    private String label;
    private long totalUsers;
}
