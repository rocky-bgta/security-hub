package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface ContentController {
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<?>>>>> getAllContents(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) com.aspire.asat.cms.dto.enums.CommonStatus status,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<?>>> getContentById(@PathVariable("id") String contentId, HttpServletRequest request);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<?>>> deleteContentById(@PathVariable("id") String contentId);

    @PostMapping
    ResponseEntity<ApiResponseDto<ContentRespDto<?>>> createContent(@RequestBody ContentReqDto<?> contentReqDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<?>>> updateContent(@PathVariable("id") String contentId, @RequestBody ContentReqDto<?> contentReqDto);

    // ========== CLIENT-FACING CONTENT APIs ==========
    
    @Operation(summary = "Mark content as completed", description = "Marks a content as completed/viewed by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content marked completed"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Content not found")
    })
    @PostMapping("/client/contents/complete")
    ResponseEntity<ApiResponseDto<Void>> updateContentStatus(
            @RequestParam @NotBlank String contentId,
            @RequestParam @NotBlank String topicId,
            @RequestParam @NotBlank String subPackageId,
            @RequestParam @NotBlank String userId);

}

