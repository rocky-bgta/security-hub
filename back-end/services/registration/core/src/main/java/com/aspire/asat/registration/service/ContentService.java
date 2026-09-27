package com.aspire.asat.registration.service;


import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.data.ContentDto;

import java.util.List;

public interface ContentService {

    List<ContentDto> getAllContent(Integer offset, Integer pageSize);

    ContentDto save(ContentDto saveContentDto);

    ApiResponse<List<ContentCountDto>> getTypeCount();

    ContentDto updateContent(ContentDto toBeUpdate);

}
