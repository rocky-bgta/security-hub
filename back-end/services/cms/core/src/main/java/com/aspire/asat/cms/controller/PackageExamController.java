package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import com.aspire.asat.cms.dto.exam.PackageExamListResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(WebApiUrlConstants.PACKAGE_EXAM_API)
public interface PackageExamController {

    @Operation(summary = "Get all package exams")
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PackageExamResponseDto>>>> getAllExams(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    );

    @GetMapping("/search")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PackageExamListResponseDto>>>> searchExams(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "pageSize", defaultValue = "10") int limit
    );

    @Operation(summary = "Update a package exam by ID")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<PackageExamResponseDto>> updateExam(
            @PathVariable String id,
            @RequestBody PackageExamCreateDto dto
    );
}
