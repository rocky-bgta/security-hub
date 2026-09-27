package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.userLicence.UserLicenceResponseDto;
import com.aspire.asat.cms.dto.userLicence.BulkUserLicenceRequestDto;

import java.util.List;

public interface UserLicenceService {

    /**
     * Create multiple user licences in bulk
     */
    List<UserLicenceResponseDto> createBulkUserLicences(BulkUserLicenceRequestDto requestDto);
}
