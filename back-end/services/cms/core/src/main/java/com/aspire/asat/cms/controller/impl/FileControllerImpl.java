package com.aspire.asat.cms.controller.impl;


import com.aspire.asat.cms.controller.FileController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.CloudFrontCookiesResponse;
import com.aspire.asat.common.dto.files.PresignUrlResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import com.aspire.asat.common.service.files.FileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileControllerImpl implements FileController {
    private final FileService fileService;
    private final FileProps fileProps;
    @PostMapping("/generate-presign")
    public ResponseEntity<ApiResponseDto<PresignedUrlGenerationResponse>> prepare(@Valid @RequestBody PresignedUrlGenerationRequest req) {
        log.info("[DEPLOY-CHECK] CMS image active — cloudFront.distributionDomain={}",
                fileProps.getAws().getCloudFront().getDistributionDomain());
        PresignedUrlGenerationResponse response = fileService.prepare(req);
        return ResponseEntity.ok(new ApiResponseDto<>("Dashboard summary fetched", 200, response));
    }

    @GetMapping("/get-presigned-url")
    public ResponseEntity<ApiResponseDto<PresignUrlResponse>> getPresignedUrl(@RequestParam String key) {
        String url = fileService.buildUrl(key);
        PresignUrlResponse urlResponse = PresignUrlResponse
                .builder()
                .url(url)
                .build();
        return ResponseEntity.ok(new ApiResponseDto<>("Presigned URL generated", 200, urlResponse));
    }

    @GetMapping("/get-signed-cookies")
    @Override
    public ResponseEntity<ApiResponseDto<CloudFrontCookiesResponse>> getSignedCookies(HttpServletResponse response) {
        try {
            Map<String, String> cookies = fileService.generateSignedCookies();
            String cookieDomain = fileProps.getAws().getCloudFront().getDistributionMainDomain();
            cookies.forEach((name, value) -> {
                ResponseCookie cookie = ResponseCookie.from(name, value)
                        .domain(cookieDomain) // exact subdomain
                        .path("/")
                        .secure(true)
                        .httpOnly(true)
                        .sameSite("None")
                        .maxAge(2 * 60 * 60)
                        .build();

                response.addHeader("Set-Cookie", cookie.toString());
            });

            CloudFrontCookiesResponse responseData = CloudFrontCookiesResponse.builder()
                    .cloudFrontPolicy(cookies.get("CloudFront-Policy"))
                    .cloudFrontSignature(cookies.get("CloudFront-Signature"))
                    .cloudFrontKeyPairId(cookies.get("CloudFront-Key-Pair-Id"))
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "CloudFront signed cookies generated",
                    200,
                   null
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>(
                            "Failed to generate signed cookies: " + e.getMessage(),
                            500,
                            null
                    ));
        }
    }

}
