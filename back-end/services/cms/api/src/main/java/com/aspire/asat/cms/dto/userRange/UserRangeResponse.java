package com.aspire.asat.cms.dto.userRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRangeResponse {
    private String id;
    private String rangeName;
    private Integer minUsers;
    private Integer maxUsers;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;
    private Boolean isDefault;
}

