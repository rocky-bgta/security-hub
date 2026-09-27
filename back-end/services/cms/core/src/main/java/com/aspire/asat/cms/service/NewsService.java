package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.news.NewsDto;
import com.aspire.asat.cms.dto.news.NewsReactionRequestDto;
import com.aspire.asat.cms.dto.news.UserReaction;

import java.util.List;

public interface NewsService {
    List<NewsDto> getNewsData(String newsId);
    UserReaction handleUserReaction(String userId, String newsId);
    String sendUserReaction(NewsReactionRequestDto newsReactionRequestDto);
}