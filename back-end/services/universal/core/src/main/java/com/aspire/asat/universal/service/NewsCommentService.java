package com.aspire.asat.universal.service;

import com.aspire.asat.universal.news.NewsCommentDto;
import com.aspire.asat.universal.news.NewsCommentRequest;

import java.util.List;
import java.util.UUID;

public interface NewsCommentService {
    List<NewsCommentDto> getCommentsForNews(UUID newsId);
    NewsCommentDto createComment(UUID newsId, NewsCommentRequest request);
}

