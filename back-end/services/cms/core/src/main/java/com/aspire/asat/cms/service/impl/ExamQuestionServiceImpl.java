package com.aspire.asat.cms.service.impl;


import com.aspire.asat.cms.dto.exam.ExamQuestionResponseDTO;
import com.aspire.asat.cms.dto.exam.QuestionType;
import com.aspire.asat.cms.service.ExamQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ExamQuestionServiceImpl implements ExamQuestionService {

    @Override
    public List<ExamQuestionResponseDTO> parseExamQuestionsFromCSV(MultipartFile file) {
        List<ExamQuestionResponseDTO> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new RuntimeException("Empty CSV file");

            String[] headers = headerLine.split(",", -1);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1); // handle empty columns

                String questionText = fields[headerIndex.get("Question Title")].trim();
                List<String> options = new ArrayList<>();
                List<String> correctAnswers = new ArrayList<>();

                for (int i = 1; i <= 10; i++) {
                    String optKey = "Option" + i;
                    String correctKey = "Is_correct" + i;

                    if (headerIndex.containsKey(optKey) && !fields[headerIndex.get(optKey)].isBlank()) {
                        String option = fields[headerIndex.get(optKey)].trim();
                        options.add(option);

                        if (headerIndex.containsKey(correctKey)) {
                            String correctness = fields[headerIndex.get(correctKey)].trim();
                            if ("TRUE".equalsIgnoreCase(correctness)) {
                                correctAnswers.add(option);
                            }
                        }
                    }
                }

                QuestionType type = QuestionType.UNKNOWN;
                if (headerIndex.containsKey("QuestionType")) {
                    String rawType = fields[headerIndex.get("QuestionType")].trim().toUpperCase();
                    try {
                        type = QuestionType.valueOf(rawType);
                    } catch (IllegalArgumentException ignored) {
                        // leave as UNKNOWN
                    }
                }

                ExamQuestionResponseDTO dto = ExamQuestionResponseDTO.builder()
                        .text(questionText)
                        .options(options)
                        .correctAnswers(correctAnswers)
                        .questionType(type)
                        .build();

                questions.add(dto);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse uploaded CSV file", e);
        }

        return questions;
    }

}
