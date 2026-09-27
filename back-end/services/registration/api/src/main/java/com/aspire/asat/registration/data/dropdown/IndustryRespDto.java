package com.aspire.asat.registration.data.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryRespDto {
    private String id;
    private String organizationTypeId;
    private String code;
    private String name;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
