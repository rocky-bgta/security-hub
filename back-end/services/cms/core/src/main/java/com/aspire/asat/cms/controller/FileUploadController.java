package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RequestMapping(value = WebApiUrlConstants.STORAGE_API, produces = "application/json")
public interface FileUploadController {

    @GetMapping("/generate-upload-url")
    ResponseEntity<ApiResponseDto<Map<String, Object>>> getUploadUrl(@RequestParam String fileName);
    @GetMapping("/check-upload-status")
    ResponseEntity<ApiResponseDto<Map<String, Object>>> checkUploadStatus(@RequestParam String fileName);

}
