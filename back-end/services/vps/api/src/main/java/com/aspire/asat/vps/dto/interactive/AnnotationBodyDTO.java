package com.aspire.asat.vps.dto.interactive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationBodyDTO {
    private String id;
    private String annotation;
    private String type;
    private String purpose;
    private String value;
    private String created;
    private String updated;
    private CreatorDTO updatedBy;
    private CreatorDTO creator;
}
