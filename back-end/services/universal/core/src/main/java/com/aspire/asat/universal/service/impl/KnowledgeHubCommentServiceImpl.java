package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentRequest;
import com.aspire.asat.universal.entity.KnowledgeHubComment;
import com.aspire.asat.universal.repository.KnowledgeHubCommentRepository;
import com.aspire.asat.universal.service.KnowledgeHubCommentService;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KnowledgeHubCommentServiceImpl implements KnowledgeHubCommentService {

    private final KnowledgeHubCommentRepository knowledgeHubCommentRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Autowired
    public KnowledgeHubCommentServiceImpl(KnowledgeHubCommentRepository knowledgeHubCommentRepository,
                                          UserCurrentContextService userCurrentContextService) {
        this.knowledgeHubCommentRepository = knowledgeHubCommentRepository;
        this.userCurrentContextService = userCurrentContextService;
    }

    @Override
    public List<KnowledgeHubCommentDto> getCommentsForKnowledgeHub(UUID knowledgeHubId) {
        List<KnowledgeHubComment> rootComments = knowledgeHubCommentRepository.findByKnowledgeHubIdAndParentCommentIdIsNullOrderByCreatedAtAsc(knowledgeHubId);
        List<KnowledgeHubCommentDto> result = new ArrayList<>();
        for (KnowledgeHubComment c : rootComments) {
            KnowledgeHubCommentDto dto = toDto(c);
            // recursively fetch replies
            dto.setReplies(getReplies(c.getId()));
            result.add(dto);
        }
        return result;
    }

    private List<KnowledgeHubCommentDto> getReplies(UUID parentId) {
        List<KnowledgeHubComment> replies = knowledgeHubCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentId);
        List<KnowledgeHubCommentDto> replyDtos = new ArrayList<>();
        for (KnowledgeHubComment r : replies) {
            KnowledgeHubCommentDto rd = toDto(r);
            // recursive call for nested replies
            rd.setReplies(getReplies(r.getId()));
            replyDtos.add(rd);
        }
        return replyDtos;
    }

    @Override
    public KnowledgeHubCommentDto createComment(UUID knowledgeHubId, KnowledgeHubCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();

        KnowledgeHubComment comment = new KnowledgeHubComment();
        comment.setKnowledgeHubId(knowledgeHubId);
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            try {
                UUID parentId = UUID.fromString(request.getParentCommentId());
                // verify parent exists
                Optional<KnowledgeHubComment> parentOpt = knowledgeHubCommentRepository.findById(parentId);
                if (parentOpt.isEmpty()) {
                    throw new IllegalArgumentException("Parent comment not found: " + request.getParentCommentId());
                }
                comment.setParentCommentId(parentId);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid parentCommentId format: " + request.getParentCommentId());
            }
        }
        comment.setUserId(ctx.getUserId());
        comment.setUserName(ctx.getFullName());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        KnowledgeHubComment saved = knowledgeHubCommentRepository.save(comment);
        return toDto(saved);
    }

    private KnowledgeHubCommentDto toDto(KnowledgeHubComment c) {
        KnowledgeHubCommentDto dto = new KnowledgeHubCommentDto();
        dto.setId(c.getId() != null ? c.getId().toString() : null);
        dto.setNewsId(c.getKnowledgeHubId() != null ? c.getKnowledgeHubId().toString() : null);
        dto.setParentCommentId(c.getParentCommentId() != null ? c.getParentCommentId().toString() : null);
        dto.setUserId(c.getUserId());
        dto.setUserName(c.getUserName());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setReplies(new ArrayList<>());
        return dto;
    }
}
