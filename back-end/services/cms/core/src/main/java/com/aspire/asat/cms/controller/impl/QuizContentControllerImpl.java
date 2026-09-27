package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.QuizContentController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.quiz.QuizContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuizContentControllerImpl implements QuizContentController {
    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<QuizContentDto>>> createQuizContent(ContentReqDto<QuizContentDto> quizContentDto) {
        return null;
    }
}
