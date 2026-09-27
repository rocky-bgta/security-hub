package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.TagController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;
import com.aspire.asat.cms.dto.tag.TagStatusUpdateDto;
import com.aspire.asat.cms.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TagControllerImpl implements TagController {

    private final TagService tagService;

    @Override
    public ResponseEntity<ApiResponseDto<TagRespDto>> createTag(TagReqDto request) {
        TagRespDto created = tagService.createTag(request);
        return ResponseEntity.status(201).body(new ApiResponseDto<>("Tag created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TagRespDto>> updateTag(String id, TagReqDto request) {
        TagRespDto updated = tagService.updateTag(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Tag updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TagRespDto>> getTagById(String id) {
        TagRespDto tag = tagService.getTagById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Tag fetched successfully", 200, tag));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TagRespDto>> deleteTagById(String id) {
        TagRespDto deactivated = tagService.deleteTagById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Tag deactivated successfully", 200, deactivated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<TagRespDto>>> getAllTags() {
        List<TagRespDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(new ApiResponseDto<>("Tags fetched successfully", 200, tags));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TagRespDto>> updateTagStatus(String id, TagStatusUpdateDto statusUpdateDto) {
        TagRespDto updated = tagService.updateTagStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(new ApiResponseDto<>("Tag status updated successfully", 200, updated));
    }
}
