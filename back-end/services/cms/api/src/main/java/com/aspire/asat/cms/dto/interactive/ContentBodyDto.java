package com.aspire.asat.cms.dto.interactive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBodyDto {
    private String id;
    private String contentName;
    private CommonContentDto commonContent;
    private SpecificContentDto specificContent;
    private String time;
}

