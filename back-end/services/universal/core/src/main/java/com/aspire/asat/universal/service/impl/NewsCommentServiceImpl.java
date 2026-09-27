package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.NewsComment;
import com.aspire.asat.universal.news.NewsCommentDto;
import com.aspire.asat.universal.news.NewsCommentRequest;
import com.aspire.asat.universal.repository.NewsCommentRepository;
import com.aspire.asat.universal.service.NewsCommentService;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsCommentServiceImpl implements NewsCommentService {

    private final NewsCommentRepository newsCommentRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Autowired
    public NewsCommentServiceImpl(NewsCommentRepository newsCommentRepository,
                                  UserCurrentContextService userCurrentContextService) {
        this.newsCommentRepository = newsCommentRepository;
        this.userCurrentContextService = userCurrentContextService;
    }

    @Override
    public List<NewsCommentDto> getCommentsForNews(UUID newsId) {
        List<NewsComment> rootComments = newsCommentRepository.findByNewsIdAndParentCommentIdIsNullOrderByCreatedAtAsc(newsId);
        List<NewsCommentDto> result = new ArrayList<>();
        for (NewsComment c : rootComments) {
            NewsCommentDto dto = toDto(c);
            // recursively fetch replies
            dto.setReplies(getReplies(c.getId()));
            result.add(dto);
        }
        return result;
    }

    private List<NewsCommentDto> getReplies(UUID parentId) {
        List<NewsComment> replies = newsCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentId);
        List<NewsCommentDto> replyDtos = new ArrayList<>();
        for (NewsComment r : replies) {
            NewsCommentDto rd = toDto(r);
            // recursive call for nested replies
            rd.setReplies(getReplies(r.getId()));
            replyDtos.add(rd);
        }
        return replyDtos;
    }

    @Override
    public NewsCommentDto createComment(UUID newsId, NewsCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();

        NewsComment comment = new NewsComment();
        comment.setNewsId(newsId);
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            try {
                UUID parentId = UUID.fromString(request.getParentCommentId());
                // verify parent exists
                Optional<NewsComment> parentOpt = newsCommentRepository.findById(parentId);
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

        NewsComment saved = newsCommentRepository.save(comment);
        return toDto(saved);
    }

    private NewsCommentDto toDto(NewsComment c) {
        NewsCommentDto dto = new NewsCommentDto();
        dto.setId(c.getId() != null ? c.getId().toString() : null);
        dto.setNewsId(c.getNewsId() != null ? c.getNewsId().toString() : null);
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
