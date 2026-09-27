package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import com.aspire.asat.cms.dto.exam.PackageExamListResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamResponseDto;

import java.util.List;

public interface PackageExamService {
    List<PackageExamResponseDto> getAllExams(int offset, int limit);
    long getTotalExamCount();
    PackageExamResponseDto updateExam(String id, PackageExamCreateDto dto);
    List<PackageExamListResponseDto> searchExams(String search, int offset, int limit);
    long getTotalExamSearchCount(String search);

}
