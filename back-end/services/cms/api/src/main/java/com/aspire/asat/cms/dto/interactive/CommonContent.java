package com.aspire.asat.cms.dto.interactive;

import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonContent {
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<UUID> chapterIds;
    private List<String> tags;
}
