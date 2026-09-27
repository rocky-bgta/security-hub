package com.aspire.asat.cms.dto.topic.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Difficulty {
    private String difficultyId;
    private String difficultyName;
}
