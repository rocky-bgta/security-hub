package com.aspire.asat.cms.service.user_operations;

import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CompletedTopicResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseTopicStatsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.SubPackageStatisticsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageListResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageStatisticsDTO;
import com.aspire.asat.cms.dto.dashboard.UserDashboardTopicsProgressResponseDto;
import com.aspire.asat.cms.dto.user.UserSubPackageAssignRequest;

import java.util.List;

public interface ClientUserOperationService {


    List<ClientCourseResponseDTO> getUserCourses(String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize);

    CourseDetailsResponseDTO getCourseDetails(String courseId, String userId, String packageId);

    List<PackageResponseDTO> getUserPackages(String userId, int offset, int pageSize, String  status);

    PackageDetailsResponseDTO getPackageDetails(String packageId, String userId);

    void markContentAsCompleted(String userId, String topicId, String contentId, String subPackageId);

    long countUserCourses(String userId, String packageId, String status, String search, Boolean isSaved);

    long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved);

    long countUserPackages(String userId, String status);

    void bookmarkCourse(String courseId, String userId, String packageId, boolean isSaved);

    void assignSubPackagesToUsers(UserSubPackageAssignRequest request);

    void assignCourseToUser(String userId, String courseId);

    void assignPackageToUser(String userId, String packageId);

    void assignPackageToMultipleUsers(String clientId, List<String> userIds, String packageId);

    List<CourseTopicStatsItemDTO> getCourseTopicStats(String userId, int offset, int pageSize);

    List<SubPackageStatisticsItemDTO> getUserSubPackageStatistics(String userId, int offset, int pageSize);

    long countUserSubPackages(String userId);

    void resetUserPackage(String userId, String packageId);

    void resetUserSubPackage(String userId, String subPackageId);

    UserDashboardTopicsProgressResponseDto getUserTopicsProgress(String userId, int offset, int pageSize, String search);

    /**
     * Gets user subpackage statistics count for dashboard
     * Returns counts of subpackages by status (total, completed, exam, inProgress, notStarted)
     *
     * @param userId the ID of the user
     * @return UserSubPackageStatisticsDTO containing counts by status
     */
    UserSubPackageStatisticsDTO getUserSubPackageStatisticsCount(String userId);

    /**
     * Gets completed topics for a user with completion dates
     * Returns list of completed topics sorted by most recent completion first
     *
     * @param userId the ID of the user
     * @param offset pagination offset
     * @param pageSize page size
     * @return List of CompletedTopicResponseDTO
     */
    List<CompletedTopicResponseDTO> getCompletedTopics(String userId, int offset, int pageSize);

    /**
     * Gets total count of completed topics for a user
     *
     * @param userId the ID of the user
     * @return total count of completed topics
     */
    long countCompletedTopics(String userId);

    /**
     * Gets list of subpackage IDs and names for a user
     * Returns list of subpackages assigned to the user with their IDs and names
     *
     * @param userId the ID of the user
     * @return List of UserSubPackageListResponseDTO containing subPackageId and subPackageName
     */
    List<UserSubPackageListResponseDTO> getUserSubPackageList(String userId);

}
