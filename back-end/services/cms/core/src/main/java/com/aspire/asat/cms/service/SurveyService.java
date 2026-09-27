package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.survey.SurveyDto;
import com.aspire.asat.cms.dto.survey.SurveyUserDto;

import java.util.List;

public interface SurveyService {
    List<SurveyDto> getSurveyData(String userId);
    String sendUserReaction(SurveyUserDto surveyUserDto);
}
