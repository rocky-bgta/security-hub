package com.aspire.asat.cms.dto.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicSummaryDto {
    private String id;
    private String topicName;
    private String description;
    private TopicStatus status;
    private double topicProgress;
    private List<ChapterDto> chapterIds;
    private String thumbnailUrl;
    private Integer durationMinutes;
    private Integer totalContentCount;
    private LocalDate subPackageValidity;
    private Boolean isBookmarked;
    private String subPackageId;

    // Manual setter for isBookmarked to ensure proper method name
    public void setIsBookmarked(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }
}
