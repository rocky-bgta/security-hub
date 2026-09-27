package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news_likes")
public class NewsLike {

    @Id
    private String id;
    private String newsId;
    private String userId;
    private boolean liked;
    private LocalDateTime createdAt;
}
