package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;

import java.util.List;

public interface TagService {

    TagRespDto createTag(TagReqDto request);

    TagRespDto updateTag(String id, TagReqDto request);

    TagRespDto getTagById(String id);

    TagRespDto deleteTagById(String id);

    List<TagRespDto> getAllTags();

    TagRespDto updateTagStatus(String id, String status);
}
