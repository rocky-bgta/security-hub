package com.aspire.asat.cms.service.user_operations;

import com.aspire.asat.cms.dto.client.responseDto.DashboardSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageSummaryDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserPackageGroupedResponseDTO;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSummaryServiceImpl implements UserSummaryService {
    private final UserPackageRepositoryCustom userPackageRepositoryCustom;
    private final UserSubPackageRepository userSubPackageRepository;


    @Override
    public DashboardSummaryResponseDTO getDashboardSummary(String userId) {
        log.info("Fetching dashboard summary for userId={}", userId);

        List<UserSubPackage> userPackages = userSubPackageRepository.findByUserId(userId);

        int totalCourses = 0;
        int completedCourses = 0;
        int inProgressCourses = 0;
        int pendingCourses = 0;

        for (UserSubPackage pkg : userPackages) {
            totalCourses++;
            String status = pkg.getStatus();

            if (CourseStatus.COMPLETED.name().equalsIgnoreCase(status)) {
                completedCourses++;
            } else if (CourseStatus.IN_PROGRESS.name().equalsIgnoreCase(status) || SubPackageStatus.EXAM.name().equalsIgnoreCase(status)) {
                inProgressCourses++;
            } else {
                pendingCourses++;
            }
        }

        log.debug("Dashboard data for userId={}: totalCourses={}, completedCourses={}, inProgressCourses={}, pendingCourses={}",
                userId, totalCourses, completedCourses, inProgressCourses, pendingCourses);

        return new DashboardSummaryResponseDTO(
                totalCourses,
                completedCourses,
                inProgressCourses,
                pendingCourses,
                0 // totalCertificates is now obsolete
        );
    }

    @Override
    public UserPackageGroupedResponseDTO getGroupedUserPackages(String userId) {
        List<Document> docs = userPackageRepositoryCustom.getUserPackagesGroupedByStatus(userId);

        Map<String, List<PackageSummaryDTO>> grouped = docs.stream()
                .map(doc -> {
                    String rawStatus = doc.getString("status");
                    if (rawStatus == null) return null;

                    // Normalize status: treat "EXAM" as "IN_PROGRESS"
                    String status = SubPackageStatus.EXAM.name().equalsIgnoreCase(rawStatus) ? "IN_PROGRESS" : rawStatus;

                    return PackageSummaryDTO.builder()
                            .id(doc.getString("id"))
                            .packageName(doc.getString("subPackageName"))
                            .price(doc.get("price") instanceof Number ? ((Number) doc.get("price")).doubleValue() : 0.0)
                            .thumbnailUrl(doc.getString("thumbnailUrl"))
                            .status(status)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        PackageSummaryDTO::getStatus,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return UserPackageGroupedResponseDTO.builder()
                .groupedPackages(grouped)
                .build();
    }
}
