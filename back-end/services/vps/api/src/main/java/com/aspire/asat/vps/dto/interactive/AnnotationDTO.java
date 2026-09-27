package com.aspire.asat.vps.dto.interactive;

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
public class AnnotationDTO {
    private String id;
    private List<AnnotationBodyDTO> bodies;
    private AnnotationTargetDTO target;
}
