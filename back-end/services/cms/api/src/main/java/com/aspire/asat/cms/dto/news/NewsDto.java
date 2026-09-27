package com.aspire.asat.cms.dto.news;

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
public class NewsDto {
    private String newsId;
    private String headline;
    private List<String> images;
    private String details;
    private List<String> tags;
    private NewsReaction reaction;
    private Integer likeCount;
    private Integer dislikeCount;
    private String thumbnailUrl;
    private List<String> category;
    private Instant createdAt;
}
