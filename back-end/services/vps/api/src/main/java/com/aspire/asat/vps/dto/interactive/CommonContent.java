package com.aspire.asat.vps.dto.interactive;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonContent {
    private String contentName;
    private String description;
    private String contentType;
    private String status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;
}
