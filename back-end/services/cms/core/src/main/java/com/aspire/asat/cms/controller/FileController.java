package com.aspire.asat.cms.controller;


import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.dto.files.CloudFrontCookiesResponse;
import com.aspire.asat.common.dto.files.PresignUrlResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public interface FileController {

    @PostMapping("/generate-presign")
     ResponseEntity<ApiResponseDto<PresignedUrlGenerationResponse>> prepare(@Valid @RequestBody PresignedUrlGenerationRequest req) ;

    @GetMapping("/get-presigned-url")
     ResponseEntity<ApiResponseDto<PresignUrlResponse>> getPresignedUrl(@RequestParam String key);

    @GetMapping("/get-signed-cookies")
     ResponseEntity<ApiResponseDto<CloudFrontCookiesResponse>>  getSignedCookies(HttpServletResponse response);


}
