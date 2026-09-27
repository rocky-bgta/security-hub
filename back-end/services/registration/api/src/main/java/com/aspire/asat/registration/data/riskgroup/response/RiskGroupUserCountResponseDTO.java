package com.aspire.asat.registration.data.riskgroup.response;

import com.aspire.asat.registration.data.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskGroupUserCountResponseDTO {

    /** Null when users have no risk group assigned. */
    private RiskGroup riskGroup;
    private long userCount;
}
