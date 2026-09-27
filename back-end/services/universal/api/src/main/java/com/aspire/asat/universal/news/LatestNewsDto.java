package com.aspire.asat.universal.news;


import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestNewsDto {
    private UUID id;
    private String name;
    private String slug;
    private String categoryId;
    private String content;
    private int sequence;
    private String imageUrl;
    private String videoUrl;
    private LocalDateTime publishedDate;
    private LocalDateTime expireDate;
    private Status status;
    private int likeCount;
    private int dislikeCount;
    private CategoryDto category;

    // User's like status: true = liked, false = disliked, null = no action
    private Boolean userLikeStatus;

    private List<NewsCommentDto> comments; // comments and replies (nested)
}
