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
public class StateRespDto {
    private String id;
    private String countryId;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
