package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;

import java.util.List;

public interface ContentService {
    List<ContentRespDto<?>> getAllContents(String search, com.aspire.asat.cms.dto.enums.CommonStatus status, Integer offset, Integer pageSize, String sortBy, String order);
    ContentRespDto<?> getContentById(String contentId);
    long getTotalContentCount();
    long getTotalContentCount(String search, com.aspire.asat.cms.dto.enums.CommonStatus status);
    ContentRespDto<?> deleteContentById(String contentId);
    ContentRespDto<?> createContent(ContentReqDto<?> contentReqDto);
    ContentRespDto<?> updateContent(String contentId, ContentReqDto<?> contentReqDto);
}
