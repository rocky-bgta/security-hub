package com.aspire.asat.universal.knowledgehub;


import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.enums.ResourceType;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.universal.dto.TagDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeHubDto {
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
    private ResourceType resourceType;
    private boolean allowDownload;
    private CategoryDto category;
    private List<TagDto> tags; // List of tag objects

    private List<KnowledgeHubCommentDto> comments; // comments and replies (nested)
}
