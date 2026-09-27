package com.aspire.asat.cms.dto.chapter;

import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.enums.ChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponseWithItemDto {
    private String id;
    private String topicId;
    private String chapterName;
    private String chapterDescription;
    private Integer position;
    private ChapterStatus chapterStatus;
    private List<ContentRespDto<?>> contentIds;
    private Instant createdAt;
    private Instant updatedAt;
}
