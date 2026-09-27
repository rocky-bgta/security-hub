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
public class MspTypeRespDto {
    private String id;
    private String name;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
