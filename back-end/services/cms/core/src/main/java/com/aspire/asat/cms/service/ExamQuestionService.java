package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.exam.ExamQuestionResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExamQuestionService {

    List<ExamQuestionResponseDTO> parseExamQuestionsFromCSV(MultipartFile file);

}
