package com.aspire.asat.cms.controller.impl.topic;

import com.aspire.asat.cms.controller.topic.ContentTypeController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.ContentTypeReqDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;
import com.aspire.asat.cms.service.topic.ContentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ContentTypeControllerImpl implements ContentTypeController {

    private final ContentTypeService contentTypeService;

    @Override
    public ResponseEntity<ApiResponseDto<ContentTypeRespDto>> createContentType(ContentTypeReqDto contentTypeReqDto) {
        ContentTypeRespDto createdContentType = contentTypeService.createContentType(contentTypeReqDto);
        ApiResponseDto<ContentTypeRespDto> response = new ApiResponseDto<>("Content type created successfully", 201, createdContentType);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentTypeRespDto>> updateContentType(String id, ContentTypeReqDto contentTypeReqDto) {
        ContentTypeRespDto updatedContentType = contentTypeService.updateContentType(id, contentTypeReqDto);
        ApiResponseDto<ContentTypeRespDto> response = new ApiResponseDto<>("Content type updated successfully", 200, updatedContentType);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentTypeRespDto>> getContentTypeById(String id) {
        ContentTypeRespDto contentType = contentTypeService.getById(id);
        ApiResponseDto<ContentTypeRespDto> response = new ApiResponseDto<>("Content type fetched successfully", 200, contentType);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteContentTypeById(String id) {
        contentTypeService.deleteContentTypeById(id);
        ApiResponseDto<Void> response = new ApiResponseDto<>("Content type deleted successfully", 200, null);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ContentTypeRespDto>>> getAllContentTypes() {
        List<ContentTypeRespDto> contentTypes = contentTypeService.getAllContentTypes();
        ApiResponseDto<List<ContentTypeRespDto>> response = new ApiResponseDto<>("Content types fetched successfully", 200, contentTypes);
        return ResponseEntity.ok(response);
    }
}
