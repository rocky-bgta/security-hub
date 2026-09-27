package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;
import com.aspire.asat.cms.dto.enums.PhishingCourseDashboardStatus;
import com.aspire.asat.cms.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.custom.PhishingCourseRepositoryCustom;
import com.aspire.asat.cms.util.PhishingCourseStatusResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PhishingCourseRepositoryCustomImpl implements PhishingCourseRepositoryCustom {

    static final List<String> PHISHING_ASSIGNED_FOR_VALUES = Arrays.stream(SubPackageAssignedFor.values())
            .map(SubPackageAssignedFor::name)
            .toList();

    private final MongoTemplate mongoTemplate;

    @Override
    public PhishingCourseStatisticsCountsDto getStatisticsCounts(String clientAdminId) {
        return getStatisticsCounts(clientAdminId, null);
    }

    @Override
    public PhishingCourseStatisticsCountsDto getStatisticsCounts(
            String clientAdminId, Collection<String> subPackageIds) {
        if (subPackageIds != null && subPackageIds.isEmpty()) {
            return PhishingCourseStatisticsCountsDto.builder()
                    .totalUsers(0)
                    .completedUsers(0)
                    .inProgressUsers(0)
                    .pendingUsers(0)
                    .expiredUsers(0)
                    .build();
        }
        Query query = buildEnrollmentQuery(clientAdminId, null, subPackageIds);
        List<UserSubPackage> enrollments = mongoTemplate.find(query, UserSubPackage.class);

        long completed = 0;
        long inProgress = 0;
        long pending = 0;
        long expired = 0;

        for (UserSubPackage enrollment : enrollments) {
            switch (PhishingCourseStatusResolver.resolve(enrollment)) {
                case complete -> completed++;
                case InProgress -> inProgress++;
                case pending -> pending++;
                case expired -> expired++;
            }
        }

        return PhishingCourseStatisticsCountsDto.builder()
                .totalUsers(enrollments.size())
                .completedUsers(completed)
                .inProgressUsers(inProgress)
                .pendingUsers(pending)
                .expiredUsers(expired)
                .build();
    }

    @Override
    public List<PhishingCourseEnrollmentDto> getDetails(String clientAdminId, int offset, int pageSize, Collection<String> userIds) {
        Query query = buildEnrollmentQuery(clientAdminId, userIds, null);
        query.with(Sort.by(Sort.Direction.DESC, "assignedDate"));
        query.skip((long) offset * pageSize);
        query.limit(pageSize);

        List<UserSubPackage> enrollments = mongoTemplate.find(query, UserSubPackage.class);
        return enrollments.stream()
                .map(this::toEnrollmentDto)
                .toList();
    }

    @Override
    public long countDetails(String clientAdminId, Collection<String> userIds) {
        Query query = buildEnrollmentQuery(clientAdminId, userIds, null);
        return mongoTemplate.count(query, UserSubPackage.class);
    }

    private Query buildEnrollmentQuery(
            String clientAdminId, Collection<String> userIds, Collection<String> subPackageIds) {
        List<String> phishingSubPackageIds = findPhishingSubPackageIds(clientAdminId);
        if (subPackageIds != null && !subPackageIds.isEmpty()) {
            List<String> requested = subPackageIds.stream()
                    .filter(id -> id != null && !id.isBlank())
                    .distinct()
                    .toList();
            phishingSubPackageIds = phishingSubPackageIds.stream()
                    .filter(requested::contains)
                    .toList();
            if (phishingSubPackageIds.isEmpty()) {
                phishingSubPackageIds = List.of("__none__");
            }
        }

        Criteria criteria = Criteria.where("clientAdminId").is(clientAdminId)
                .and("subPackageId").in(phishingSubPackageIds);

        if (userIds != null && !userIds.isEmpty()) {
            criteria = criteria.and("userId").in(userIds);
        }

        return new Query(criteria);
    }

    private List<String> findPhishingSubPackageIds(String clientAdminId) {
        Query subPackageQuery = new Query(
                Criteria.where("clientAdminId").is(clientAdminId)
                        .and("assignedFor").in(PHISHING_ASSIGNED_FOR_VALUES)
                        .and("deleted").is(false)
        );
        subPackageQuery.fields().include("_id");

        List<SubPackage> subPackages = mongoTemplate.find(subPackageQuery, SubPackage.class);
        List<String> ids = subPackages.stream().map(SubPackage::getId).toList();

        if (ids.isEmpty()) {
            log.debug("No phishing sub-packages found for clientAdminId={}", clientAdminId);
            return List.of("__none__");
        }
        return ids;
    }

    private PhishingCourseEnrollmentDto toEnrollmentDto(UserSubPackage enrollment) {
        PhishingCourseDashboardStatus dashboardStatus = PhishingCourseStatusResolver.resolve(enrollment);
        return PhishingCourseEnrollmentDto.builder()
                .userId(enrollment.getUserId())
                .subPackageId(enrollment.getSubPackageId())
                .subPackageName(enrollment.getSubPackageName())
                .assignedDate(enrollment.getAssignedDate())
                .expiryDate(enrollment.getExpiryDate())
                .status(dashboardStatus.name())
                .progress(enrollment.getProgress())
                .build();
    }
}
