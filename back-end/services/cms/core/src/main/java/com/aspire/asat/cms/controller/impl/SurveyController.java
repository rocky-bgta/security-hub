package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.leaderboard.LeaderboardDTO;
import com.aspire.asat.cms.dto.survey.SurveyDto;
import com.aspire.asat.cms.dto.survey.SurveyUserDto;
import com.aspire.asat.cms.service.LeaderboardService;
import com.aspire.asat.cms.service.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/api/v1/survey", produces = "application/json")
public class SurveyController {

    private SurveyService surveyServices;

    public SurveyController(SurveyService surveyServices) {
        this.surveyServices = surveyServices;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<SurveyDto>>> getMappedSurvey(@RequestParam String userId) {
        // Call the service method to get data and return it as a reactive Flux
        List<SurveyDto> leaderboardData = surveyServices.getSurveyData(userId);
        ApiResponseDto<List<SurveyDto>> response = new ApiResponseDto<>("Survey retrieved successfully", HttpStatus.OK.value(),leaderboardData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<String>> createSurvey(@RequestBody SurveyUserDto surveyUserDto) {
        String  response = surveyServices.sendUserReaction(surveyUserDto);
        ApiResponseDto<String> apiResponseDto = new ApiResponseDto<>("Survey submitted successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
    }


}
