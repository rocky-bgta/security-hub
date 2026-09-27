package com.aspire.asat.universal.policy;

import com.aspire.asat.universal.enums.PolicyTypeCode;
import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTypeRequest {
    private String name;
    private PolicyTypeCode code;
    private Status status;
}

