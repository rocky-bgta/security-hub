package com.aspire.asat.universal.knowledgehub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeHubCommentRequest {
    private String content;
    private String parentCommentId; // optional - for replies
}

