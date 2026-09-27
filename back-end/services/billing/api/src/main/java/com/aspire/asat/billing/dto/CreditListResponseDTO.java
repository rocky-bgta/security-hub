package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Credit List Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditListResponseDTO {
    private List<CreditResponseDTO> credits;
    private int totalCount;
}
