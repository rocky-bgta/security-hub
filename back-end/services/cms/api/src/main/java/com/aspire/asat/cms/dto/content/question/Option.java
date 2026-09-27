package com.aspire.asat.cms.dto.content.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Option {

    private String option;
    private Boolean isCorrect;

}
