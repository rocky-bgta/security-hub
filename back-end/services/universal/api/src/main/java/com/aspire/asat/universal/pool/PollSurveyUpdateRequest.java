package com.aspire.asat.universal.pool;


import com.aspire.asat.universal.enums.PollSurveyStatus;
import com.aspire.asat.universal.enums.PollSurveyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyUpdateRequest {
    private String title;
    private String description;
    private PollSurveyType type;
    private PollSurveyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean showResultsToUsers;
    private Boolean allowMultipleSubmissions;
    private List<PollSurveyQuestionCreateDto> questions;
}
