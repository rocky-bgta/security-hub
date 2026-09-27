package com.aspire.asat.universal.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCommentRequest {
    private String content;
    private String parentCommentId; // optional - for replies
}

