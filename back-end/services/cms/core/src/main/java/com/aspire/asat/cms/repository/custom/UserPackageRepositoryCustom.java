package com.aspire.asat.cms.repository.custom;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface UserPackageRepositoryCustom {
    List<Document> getUserPackagesWithDetails(String userId, int offset, int pageSize, String status);

    long countUserPackagesWithDetails(String userId, String status);

    List<Document> getUserPackagesWithCourseIncluded(String userId, String courseId);

    List<Document> getUserPackagesWithCourseIds(String userId, int offset, int pageSize);

    List<Document> getUserCourseStatuses(String userId, List<String> courseIds);

    List<Document> getUserPackageStatsMinimal(String userId);

    List<Document> getUserCertificates(String userId, String subPackageId, int offset, int pageSize);

    long countUserCertificates(String userId, String subPackageId);

    List<Document> getUserPackagesGroupedByStatus(String userId);

    List<Document> getClientProductStatsByPackageIds(List<String> packageIds, List<String> endUserIds, String search);

    List<Document> getAnalyticsByPackageIdsAndUserIds(List<String> packageIds, List<String> endUserIds);

    Map<String, String> getProductNamesByProductIds(List<String> productIds);

    Map<String, String> getBundleNamesByPackageIds(List<String> packageIds);

    List<Document> getPackagePerformanceStats(List<String> productIds, List<String> endUserIds);

    List<Document> searchExamsWithPackageName(String search, int offset, int limit);

    long countExamsWithSearch(String search);


}
