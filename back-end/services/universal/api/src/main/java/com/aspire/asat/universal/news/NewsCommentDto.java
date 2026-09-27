package com.aspire.asat.universal.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCommentDto {
    private String id;
    private String newsId;
    private String parentCommentId;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NewsCommentDto> replies;
}

