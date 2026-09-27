package com.aspire.asat.cms.dto.interactive;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PairDTO {
    private String inputTypeLeft;
    private String inputTypeRight;
    private String valueLeft;
    private String valueRight;
    private String id;
    private String linkLeft;
    private String linkRight;
}
