package com.aspire.asat.cms.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedProductTagDto {
    private String productId;
    private String productName;
    private List<String> tags;
}
