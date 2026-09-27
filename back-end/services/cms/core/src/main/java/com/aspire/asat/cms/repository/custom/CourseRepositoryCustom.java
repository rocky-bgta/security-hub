package com.aspire.asat.cms.repository.custom;

import org.bson.Document;

import java.util.List;
import java.util.UUID;

public interface CourseRepositoryCustom {
    List<Document> getUserCourses(String userId, String status, String search, Boolean isSaved, int offset, int pageSize);
    List<Document> getUserCoursesByPackage(String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize);
    long countUserCourses(String userId, String status, String search, Boolean isSaved);
    long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved);
    List<Document> getUserCertificates(String userId, String courseId, int offset, int pageSize);
    long countUserCertificates(String userId, String courseId);
    Document getUserDashboardSummary(String userId);
    Document getFullCourseHierarchy(String userId, String courseId, String packageId);


}
