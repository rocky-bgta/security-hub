package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.quiz.QuizContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface QuizContentController {

    @PostMapping(value = "/quiz")
    ResponseEntity<ApiResponseDto<ContentRespDto<QuizContentDto>>> createQuizContent(@RequestBody ContentReqDto<QuizContentDto> quizContentDto);
}
