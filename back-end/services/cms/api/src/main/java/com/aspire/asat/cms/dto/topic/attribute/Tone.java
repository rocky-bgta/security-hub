package com.aspire.asat.cms.dto.topic.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tone {
    private String toneId;
    private String toneName;
}
