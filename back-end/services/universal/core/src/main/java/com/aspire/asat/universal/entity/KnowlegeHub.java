package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aspire.asat.universal.enums.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowlege_hub")
public class KnowlegeHub {

    @Id
    private UUID id = UUID.randomUUID();   // ✅ Auto-generate UUID on creation

    private String name;

    @Indexed(unique = true)
    private String slug;

    private String categoryId;
    private String content;
    private int sequence;
    private String imageUrl;
    private String videoUrl;
    private LocalDateTime publishedDate;
    private LocalDateTime expireDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Status status;
    private int likeCount;
    private int dislikeCount;
    private ResourceType resourceType;
    private boolean allowDownload;

    @DBRef
    private Category category;
}
