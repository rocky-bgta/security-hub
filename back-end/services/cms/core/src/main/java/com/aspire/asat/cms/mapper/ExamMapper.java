package com.aspire.asat.cms.mapper;

import com.aspire.asat.cms.dto.enums.ExamStatus;
import com.aspire.asat.cms.dto.exam.ExamCreationResponseDto;
import com.aspire.asat.cms.dto.exam.TopicExamInfoDto;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.topic.Topic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamMapper {

    /**
     * Maps PackageExam entity to ExamCreationResponseDto
     */
    public ExamCreationResponseDto toExamCreationResponseDto(Exams exams, SubPackage subPackage, List<Topic> topics, List<Integer> questionCountsPerTopic) {
        List<TopicExamInfoDto> topicInfoList = topics.stream()
                .map(topic -> {
                    int index = topics.indexOf(topic);
                    int questionCount = index < questionCountsPerTopic.size() ? questionCountsPerTopic.get(index) : 0;

                    return TopicExamInfoDto.builder()
                            .topicId(topic.getId())
                            .topicName(topic.getTopicName())
                            .questionCount(questionCount)
                            .totalAvailableQuestions(0) // This will be set by the service
                            .build();
                })
                .toList();

        return ExamCreationResponseDto.builder()
                .examId(exams.getExamId())
                .examTitle(exams.getTitle())
                .examDescription(exams.getExamDetails())
                .subPackageId(subPackage.getId())
                .subPackageName(subPackage.getName())
                .passingScore(exams.getPassingScore())
                .topics(topicInfoList)
                .createdAt(exams.getCreatedAt())
                .totalQuestions(exams.getTotalQuestions())
                .status(ExamStatus.CREATED.name())
                .build();
    }

    /**
     * Maps Topic entity to TopicExamInfoDto
     */
    public TopicExamInfoDto toTopicExamInfoDto(Topic topic, int questionCount, int totalAvailableQuestions) {
        return TopicExamInfoDto.builder()
                .topicId(topic.getId())
                .topicName(topic.getTopicName())
                .questionCount(questionCount)
                .totalAvailableQuestions(totalAvailableQuestions)
                .build();
    }
}
