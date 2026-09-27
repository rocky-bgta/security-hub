package com.aspire.asat.registration.data.mspUser.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditUpdateDto {
    private Boolean enableCredit;
    private String reason;
    private BigDecimal creditAmount;
    private String netDaysId;
    private Date creditStartDate;
    private Boolean autoSuspendOnOverdue;
}
