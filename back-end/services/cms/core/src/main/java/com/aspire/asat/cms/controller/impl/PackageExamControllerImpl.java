package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.PackageExamController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import com.aspire.asat.cms.dto.exam.PackageExamListResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamResponseDto;
import com.aspire.asat.cms.service.PackageExamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PackageExamControllerImpl implements PackageExamController {

    private final PackageExamService service;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PackageExamResponseDto>>>> getAllExams(int offset, int limit) {
        List<PackageExamResponseDto> exams = service.getAllExams(offset, limit);
        long total = service.getTotalExamCount();
        AllResponseDto<List<PackageExamResponseDto>> response = new AllResponseDto<>(offset, limit, total, exams);
        return ResponseEntity.ok(new ApiResponseDto<>("Exams fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PackageExamListResponseDto>>>> searchExams(String search, int offset, int limit) {
        List<PackageExamListResponseDto> exams = service.searchExams(search, offset, limit);
        long total = service.getTotalExamSearchCount(search);

        AllResponseDto<List<PackageExamListResponseDto>> allResponse = new AllResponseDto<>(offset, limit, total, exams);

        ApiResponseDto<AllResponseDto<List<PackageExamListResponseDto>>> response =
                new ApiResponseDto<>("Exams retrieved successfully", HttpStatus.OK.value(), allResponse);

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ApiResponseDto<PackageExamResponseDto>> updateExam(String id, PackageExamCreateDto dto) {
        PackageExamResponseDto updated = service.updateExam(id, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Exam updated successfully", 200, updated));
    }
}
