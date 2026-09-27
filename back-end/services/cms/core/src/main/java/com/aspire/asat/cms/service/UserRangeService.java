package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.userRange.UserRangeRequest;
import com.aspire.asat.cms.dto.userRange.UserRangeResponse;

import java.util.List;

public interface UserRangeService {

    UserRangeResponse createUserRange(UserRangeRequest request);

    List<UserRangeResponse> getAllUserRanges(Boolean isActive);

    UserRangeResponse getUserRangeById(String id);

    UserRangeResponse updateUserRange(String id, UserRangeRequest request);

    void deleteUserRange(String id);
}

