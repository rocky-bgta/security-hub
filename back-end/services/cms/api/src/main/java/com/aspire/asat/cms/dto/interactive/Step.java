package com.aspire.asat.cms.dto.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step {
    private TabSectionAdditionalProperties additionalProperties; //it is correct //TabSectionAdditionalProperties
    private List<Story> stories;
    private List<ButtonDto> buttons;
}
