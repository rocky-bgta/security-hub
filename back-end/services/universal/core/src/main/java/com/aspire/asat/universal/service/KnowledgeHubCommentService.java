package com.aspire.asat.universal.service;

import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentRequest;

import java.util.List;
import java.util.UUID;

public interface KnowledgeHubCommentService {
    List<KnowledgeHubCommentDto> getCommentsForKnowledgeHub(UUID knowledgeHubId);
    KnowledgeHubCommentDto createComment(UUID knowledgeHubId, KnowledgeHubCommentRequest request);
}
