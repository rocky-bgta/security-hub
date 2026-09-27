package com.aspire.asat.vps.service;

import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;


public interface AzureStorageService {
    ResponseDto uploadVideosToCloud(String localDirPath, RequestDto requestDto, String jobId, String currentDate) throws Exception;
}
