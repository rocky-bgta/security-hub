package com.aspire.asat.cms.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AspireUserBasicDto {

    private String userId;
    private String email;
    private String fullName;
    private String department;
    private String riskGroup;
}
