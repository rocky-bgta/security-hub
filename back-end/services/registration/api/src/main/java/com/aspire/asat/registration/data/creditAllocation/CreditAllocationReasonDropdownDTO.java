package com.aspire.asat.registration.data.creditAllocation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditAllocationReasonDropdownDTO {
    private String id;
    private String reasonName;
    private String description;
}

