package com.aspire.asat.cms.dto.topic.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadType {
    private String payloadTypeId;
    private String payloadTypeName;
}
