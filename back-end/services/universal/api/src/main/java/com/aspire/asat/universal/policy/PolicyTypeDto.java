package com.aspire.asat.universal.policy;

import com.aspire.asat.universal.enums.PolicyTypeCode;
import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTypeDto {
    private String id;
    private String name;
    private PolicyTypeCode code;
    private Status status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

