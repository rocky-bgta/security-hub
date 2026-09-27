package com.aspire.asat.cms.service.topic;


import com.aspire.asat.cms.dto.topic.ContentTypeReqDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;

import java.util.List;

public interface ContentTypeService {
    ContentTypeRespDto createContentType(ContentTypeReqDto contentTypeReqDto);
    ContentTypeRespDto updateContentType(String id, ContentTypeReqDto contentTypeReqDto);
    ContentTypeRespDto getById(String id);
    void deleteContentTypeById(String id);
    List<ContentTypeRespDto> getAllContentTypes();
}
