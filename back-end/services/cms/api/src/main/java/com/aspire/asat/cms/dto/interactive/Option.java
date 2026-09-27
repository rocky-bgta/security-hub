package com.aspire.asat.cms.dto.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Option {
    private String option;

    @JsonProperty("isCorrect")
    private boolean correct;

    private String id;
    private Integer index;
    private String optionText;
    private String optionImage;
    private String optionImageLink;
}
