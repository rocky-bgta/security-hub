package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageReqDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageRespDto;

import java.util.List;

public interface ContentsAvailableLanguageService {

    ContentsAvailableLanguageRespDto create(ContentsAvailableLanguageReqDto dto);

    ContentsAvailableLanguageRespDto update(String id, ContentsAvailableLanguageReqDto dto);

    ContentsAvailableLanguageRespDto getById(String id);

    void deleteById(String id);

    List<ContentsAvailableLanguageRespDto> getAll(Boolean active);
}
