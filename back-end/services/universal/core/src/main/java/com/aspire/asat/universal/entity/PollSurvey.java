package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.enums.PollSurveyStatus;
import com.aspire.asat.universal.enums.PollSurveyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "poll_surveys")
public class PollSurvey {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String title;

    @Field("description")
    private String description;

    private PollSurveyType type;

    private PollSurveyStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private Boolean showResultsToUsers = false;

    @Builder.Default
    private Boolean allowMultipleSubmissions = false;

    private String createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private String updatedBy;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // embed questions inside the poll document
    @Builder.Default
    private List<PollSurveyQuestion> questions = new ArrayList<>();

    public void addQuestion(PollSurveyQuestion question) {
        question.setPollSurveyId(this.id);
        this.questions.add(question);
    }

    public void clearQuestions() {
        this.questions.clear();
    }
}
