package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ContentsAvailableLanguageController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageReqDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageRespDto;
import com.aspire.asat.cms.service.ContentsAvailableLanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ContentsAvailableLanguageControllerImpl implements ContentsAvailableLanguageController {

    private final ContentsAvailableLanguageService service;

    @Override
    public ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> create(
            @Valid @RequestBody ContentsAvailableLanguageReqDto dto) {
        ContentsAvailableLanguageRespDto created = service.create(dto);
        return ResponseEntity.status(201).body(
                new ApiResponseDto<>("Language created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> update(
            String id, ContentsAvailableLanguageReqDto dto) {
        ContentsAvailableLanguageRespDto updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Language updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> getById(String id) {
        ContentsAvailableLanguageRespDto language = service.getById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Language fetched successfully", 200, language));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteById(String id) {
        service.deleteById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Language deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ContentsAvailableLanguageRespDto>>> getAll(
            @RequestParam(value = "active", required = false) Boolean active) {
        List<ContentsAvailableLanguageRespDto> languages = service.getAll(active);
        return ResponseEntity.ok(new ApiResponseDto<>("Languages fetched successfully", 200, languages));
    }
}
