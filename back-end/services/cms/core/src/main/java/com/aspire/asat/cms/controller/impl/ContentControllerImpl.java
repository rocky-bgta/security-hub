package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.cms.controller.ContentController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.service.ContentService;
import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import com.aspire.asat.common.enums.ActivityType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ContentControllerImpl implements ContentController {
    private final ContentService contentService;
    private final ClientUserOperationService clientUserOperationService;

    @Autowired
    public ContentControllerImpl(ContentService contentService, ClientUserOperationService clientUserOperationService) {
        this.contentService = contentService;
        this.clientUserOperationService = clientUserOperationService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<?>>>>> getAllContents(String search, com.aspire.asat.cms.dto.enums.CommonStatus status, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<?>> allContents = contentService.getAllContents(search, status, offset, pageSize, sortBy, order);
        long totalCount = contentService.getTotalContentCount(search, status);
        AllResponseDto<List<ContentRespDto<?>>> allResponseDto = new AllResponseDto<>(offset, pageSize, totalCount, allContents);
        ApiResponseDto<AllResponseDto<List<ContentRespDto<?>>>> response = new ApiResponseDto<>("Contents fetched successfully", 200, allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<?>>> getContentById(String contentId, HttpServletRequest request) {
        ContentRespDto<?> content = contentService.getContentById(contentId);
        ApiResponseDto<ContentRespDto<?>> response = new ApiResponseDto<>("Content fetched successfully", 200, content);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<?>>> deleteContentById(String contentId) {
        ContentRespDto<?> content = contentService.deleteContentById(contentId);
        ApiResponseDto<ContentRespDto<?>> response = new ApiResponseDto<>("Content deleted successfully", 200, content);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created content: #{#contentReqDto.common != null && #contentReqDto.common.contentName != null ? #contentReqDto.common.contentName : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<ContentRespDto<?>>> createContent(ContentReqDto<?> contentReqDto) {
        ContentRespDto<?> savedContent = contentService.createContent(contentReqDto);
        ApiResponseDto<ContentRespDto<?>> response = new ApiResponseDto<>("Content created successfully", 201, savedContent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated content: #{#contentId}",
            oldValueExpression = "#{#contentId}",
            newValueExpression = "#{#contentReqDto.common != null && #contentReqDto.common.contentName != null ? #contentReqDto.common.contentName : #contentId}"
    )
    public ResponseEntity<ApiResponseDto<ContentRespDto<?>>> updateContent(String contentId, ContentReqDto<?> contentReqDto) {
        ContentRespDto<?> updatedContent = contentService.updateContent(contentId, contentReqDto);
        ApiResponseDto<ContentRespDto<?>> response = new ApiResponseDto<>("Content updated successfully", 200, updatedContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateContentStatus(String contentId, String topicId, String subPackageId, String userId) {
        log.info("Marking content: {} as completed for user: {} in topic: {} and subPackage: {}", 
                contentId, userId, topicId, subPackageId);
        clientUserOperationService.markContentAsCompleted(userId, topicId, contentId, subPackageId);
        return ResponseEntity.ok(new ApiResponseDto<>("Content marked as completed", 200, null));
    }
}
