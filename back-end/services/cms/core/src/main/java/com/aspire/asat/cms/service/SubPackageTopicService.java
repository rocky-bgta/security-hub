package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.bookmark.BookmarkRequestDto;
import com.aspire.asat.cms.dto.subPackage.UserSubpackageResponse;
import com.aspire.asat.cms.dto.topic.TopicSummaryDto;

import java.util.List;

public interface SubPackageTopicService {

    List<TopicSummaryDto> getTopicDetailsBySubPackageId(String subPackageId, String userId);

    List<UserSubpackageResponse> getTopicsByUserSubPackage(String userId, String status);

    boolean toggleBookmark(BookmarkRequestDto request);

    List<TopicSummaryDto> getBookmarkedTopicsByUserId(String userId);

}
