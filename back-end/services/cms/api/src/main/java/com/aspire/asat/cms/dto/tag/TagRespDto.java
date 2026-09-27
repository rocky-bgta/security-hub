package com.aspire.asat.cms.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRespDto {

    private String id;
    private String name;
    private String description;
    private Instant createdAt;
    private String createdBy;
    private String status;
}
