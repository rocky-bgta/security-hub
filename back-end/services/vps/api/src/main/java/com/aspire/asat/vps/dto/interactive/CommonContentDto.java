package com.aspire.asat.vps.dto.interactive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonContentDto {
    private String contentName;
    private String contentType;
    private String status;
    private List<String> chapterIds;
    private List<String> tags;
}
