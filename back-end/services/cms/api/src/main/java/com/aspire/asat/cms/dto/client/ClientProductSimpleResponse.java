package com.aspire.asat.cms.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for client product listing
 * Contains only essential product information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductSimpleResponse {
    private String id;
    private String productName;
    private String thumbnailUrl;
    private Integer displayOrder;
}

