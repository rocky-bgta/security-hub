package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.news.NewsDto;
import com.aspire.asat.cms.dto.news.NewsReactionRequestDto;
import com.aspire.asat.cms.dto.news.UserReaction;
import com.aspire.asat.cms.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/api/v1/news", produces = "application/json")
public class NewsController {

    private NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<NewsDto>>> getMappedNews(@RequestParam String userId) {
        // Call the service method to get data and return it as a reactive Flux
        List<NewsDto> newsData = newsService.getNewsData(userId);
        ApiResponseDto<List<NewsDto>> response = new ApiResponseDto<>("News retrieved successfully", HttpStatus.OK.value(),newsData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reaction")
    public ResponseEntity<ApiResponseDto<UserReaction>> getUserReaction(@RequestParam String userId, @RequestParam String newsId) {
        UserReaction userReaction = newsService.handleUserReaction(userId, newsId);
        ApiResponseDto<UserReaction> response = new ApiResponseDto<>("Reaction retrieved successfully", HttpStatus.OK.value(),userReaction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/react")
    public ResponseEntity<ApiResponseDto<String>> handleUserReaction(@RequestBody NewsReactionRequestDto newsReactionRequestDto) {
    String response = newsService.sendUserReaction(newsReactionRequestDto);
    ApiResponseDto<String> apiResponseDto = new ApiResponseDto<>("Reaction sent successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
    }
}
