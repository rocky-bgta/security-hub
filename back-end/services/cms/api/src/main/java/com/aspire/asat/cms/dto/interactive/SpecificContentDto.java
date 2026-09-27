package com.aspire.asat.cms.dto.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecificContentDto {
    private InteractiveVideo interactiveVideo;
    /** Multi-language videos. Accepts array from request or map from storage. For nested VIDEO/ANIMATION in contentList. */
    private Object interactiveVideoByLanguage;
    private BackgroundFormatting backgroundFormatting;
    private TitleFormatting titleFormatting;
    private SubTitleFormatting subTitleFormatting;
    private ParagraphFormatting paragraphFormatting;
    private LinkFormatting linkFormatting;
    private Metadata metadata;

    private String captionUrl;
    private String question;
    private List<Option> options;
    private boolean scorable;
    private String score;

    private List<Slide> slides;
    private SlideAdditionalProperties slideAdditionalProperties;

    private List<TabSection> tabSections;
    private TabSectionAdditionalProperties additionalProperties; //this is used in all sectors

    private String markdownText;
    private String headingColor;
    private String textColor;
    private String linkColor;

    //story block
    private List<Step> steps;

    private String featureImageLink;
    private boolean updateContent;

    //quiz
    private String quizType;
    private String labelLeft;
    private String labelRight;
    private String scenarioText;
    private List<PairDTO> pairs;
    private String imageLink;
    private List<AnnotationDTO> annotations;
    private List<QuestionDTO> questions;

    // hotspot quiz
    private String imageTitle;
    private String correctFeedback;
    private String incorrectFeedback;
    private Boolean showAutoFeedback;
    private List<String> correctTags;

    // phishing_detection / timed quizzes
    private Integer timeLimitSeconds;
    private List<PhishingEmailDTO> emails;
}
