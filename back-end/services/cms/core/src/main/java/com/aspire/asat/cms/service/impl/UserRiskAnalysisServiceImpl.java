package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.clientDashboard.UserRiskAnalysisResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskDetailDto;
import com.aspire.asat.cms.dto.enums.RiskCategory;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.UserRiskAnalysisService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.util.ExcelGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for User Risk Analysis
 * Provides business logic for analyzing user risk based on training completion progress
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRiskAnalysisServiceImpl implements UserRiskAnalysisService {

    private final UserSubPackageRepository userSubPackageRepository;
    private final ExamRepository examRepository;
    private final RegistrationServiceClient registrationServiceClient;
    private final ExcelGeneratorUtil excelGeneratorUtil;

    // Risk category thresholds
    private static final double SAFE_THRESHOLD = 90.0;
    private static final double LOW_RISK_THRESHOLD = 70.0;
    private static final double AVERAGE_RISK_THRESHOLD = 50.0;

    @Override
    public UserRiskAnalysisResponseDto generateUserRiskAnalysis(String clientAdminId, String productId) {
        log.info("Generating user risk analysis for client admin: {}, productId: {}", clientAdminId, productId);

        try {
            // Get all user sub-packages for the client admin, optionally filtered by productId
            List<UserSubPackage> userSubPackages;
            if (productId != null && !productId.isBlank()) {
                userSubPackages = userSubPackageRepository.findByClientAdminIdAndProductId(clientAdminId, productId);
                log.debug("Filtering by productId: {}, found {} user sub-packages", productId, userSubPackages.size());
            } else {
                userSubPackages = userSubPackageRepository.findByClientAdminId(clientAdminId);
                log.debug("No productId filter, found {} user sub-packages for client admin", userSubPackages.size());
            }
            
            if (userSubPackages.isEmpty()) {
                log.info("No user sub-packages found for client admin: {}, productId: {}", clientAdminId, productId);
                return createEmptyRiskAnalysis(clientAdminId, productId);
            }

            // Group by user ID to calculate individual user progress
            Map<String, List<UserSubPackage>> userSubPackagesMap = userSubPackages.stream()
                    .collect(Collectors.groupingBy(UserSubPackage::getUserId));

            // Calculate risk categories
            long safeUsers = 0;
            long lowRisk = 0;
            long averageRisk = 0;
            long highRisk = 0;

            for (Map.Entry<String, List<UserSubPackage>> entry : userSubPackagesMap.entrySet()) {
                String userId = entry.getKey();
                List<UserSubPackage> userSubPkgs = entry.getValue();
                
                Double overallProgress = calculateUserOverallProgress(userId, clientAdminId, productId);
                RiskCategory riskCategory = determineRiskCategory(overallProgress);

                switch (riskCategory) {
                    case SAFE -> safeUsers++;
                    case LOW_RISK -> lowRisk++;
                    case AVERAGE_RISK -> averageRisk++;
                    case HIGH_RISK -> highRisk++;
                }
            }

            long totalUsers = userSubPackagesMap.size();

            log.info("Risk analysis completed for client admin: {}, productId: {} - Total users: {}, Safe: {}, Low Risk: {}, Average Risk: {}, High Risk: {}", 
                    clientAdminId, productId, totalUsers, safeUsers, lowRisk, averageRisk, highRisk);

            return UserRiskAnalysisResponseDto.builder()
                    .safeUsers(safeUsers)
                    .lowRisk(lowRisk)
                    .averageRisk(averageRisk)
                    .highRisk(highRisk)
                    .totalUsers(totalUsers)
                    .generatedAt(Instant.now().toString())
                    .clientAdminId(clientAdminId)
                    .productId(productId)
                    .build();

        } catch (Exception e) {
            log.error("Error generating user risk analysis for client admin: {}, productId: {}", clientAdminId, productId, e);
            throw new RuntimeException("Failed to generate user risk analysis", e);
        }
    }

    @Override
    public List<UserRiskDetailDto> getUserRiskAnalysisDetails(String clientAdminId, RiskCategory riskCategory, 
                                                             Integer offset, Integer pageSize) {
        log.info("Getting detailed user risk analysis for client admin: {}, risk category: {}", 
                clientAdminId, riskCategory);

        try {
            // Get all user sub-packages for the client admin
            List<UserSubPackage> userSubPackages = userSubPackageRepository.findByClientAdminId(clientAdminId);
            
            if (userSubPackages.isEmpty()) {
                return Collections.emptyList();
            }

            // Group by user ID
            Map<String, List<UserSubPackage>> userSubPackagesMap = userSubPackages.stream()
                    .collect(Collectors.groupingBy(UserSubPackage::getUserId));

            // Convert to detailed DTOs
            List<UserRiskDetailDto> riskDetails = userSubPackagesMap.entrySet().stream()
                    .map(entry -> {
                        String userId = entry.getKey();
                        List<UserSubPackage> userSubPkgs = entry.getValue();
                        return createUserRiskDetail(userId, userSubPkgs, clientAdminId);
                    })
                    .filter(detail -> riskCategory == null || riskCategory == detail.getRiskCategory())
                    .collect(Collectors.toList());

            // Apply pagination
            if (offset != null && pageSize != null) {
                int start = offset;
                int end = Math.min(start + pageSize, riskDetails.size());
                if (start < riskDetails.size()) {
                    riskDetails = riskDetails.subList(start, end);
                } else {
                    riskDetails = Collections.emptyList();
                }
            }

            return riskDetails;

        } catch (Exception e) {
            log.error("Error getting detailed user risk analysis for client admin: {}", clientAdminId, e);
            throw new RuntimeException("Failed to get detailed user risk analysis", e);
        }
    }

    @Override
    public String exportUserRiskAnalysis(String clientAdminId) {
        log.info("Exporting user risk analysis for client admin: {}", clientAdminId);

        try {
            List<UserRiskDetailDto> riskDetails = getUserRiskAnalysisDetails(clientAdminId, null, null, null);
            
            // Generate Excel file
            String fileName = "user_risk_analysis_" + clientAdminId + "_" + System.currentTimeMillis() + ".xlsx";
            String downloadUrl = excelGeneratorUtil.generateUserRiskAnalysisExcel(riskDetails, fileName);
            
            log.info("User risk analysis exported successfully for client admin: {}, file: {}", clientAdminId, fileName);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Error exporting user risk analysis for client admin: {}", clientAdminId, e);
            throw new RuntimeException("Failed to export user risk analysis", e);
        }
    }

    @Override
    public Double calculateUserOverallProgress(String userId, String clientAdminId, String productId) {
        log.debug("Calculating overall progress for user: {} under client admin: {}, productId: {}", userId, clientAdminId, productId);

        try {
            List<UserSubPackage> userSubPackages;
            if (productId != null && !productId.isBlank()) {
                userSubPackages = userSubPackageRepository.findByUserIdAndClientAdminIdAndProductId(userId, clientAdminId, productId);
            } else {
                userSubPackages = userSubPackageRepository.findByUserIdAndClientAdminId(userId, clientAdminId);
            }
            
            if (userSubPackages.isEmpty()) {
                return 0.0;
            }

            // Calculate weighted average progress based on sub-package progress
            double totalProgress = 0.0;
            int validSubPackages = 0;

            for (UserSubPackage userSubPackage : userSubPackages) {
                // Skip expired sub-packages
                if (isSubPackageExpired(userSubPackage)) {
                    continue;
                }

                totalProgress += userSubPackage.getProgress();
                validSubPackages++;
            }

            if (validSubPackages == 0) {
                return 0.0;
            }

            double overallProgress = totalProgress / validSubPackages;
            log.debug("Overall progress calculated for user: {} - {}% (productId: {})", userId, overallProgress, productId);
            
            return Math.round(overallProgress * 100.0) / 100.0; // Round to 2 decimal places

        } catch (Exception e) {
            log.error("Error calculating overall progress for user: {} under client admin: {}, productId: {}", userId, clientAdminId, productId, e);
            return 0.0;
        }
    }

    @Override
    public RiskCategory determineRiskCategory(Double progressPercentage) {
        return RiskCategory.fromProgressPercentage(progressPercentage);
    }

    /**
     * Creates detailed risk information for a specific user
     */
    private UserRiskDetailDto createUserRiskDetail(String userId, List<UserSubPackage> userSubPackages, String clientAdminId) {
        // For detailed analysis, we don't filter by productId (analyze all products)
        Double overallProgress = calculateUserOverallProgress(userId, clientAdminId, null);
        RiskCategory riskCategory = determineRiskCategory(overallProgress);

        // Calculate sub-package status counts
        int totalSubPackages = userSubPackages.size();
        int completedSubPackages = 0;
        int inProgressSubPackages = 0;
        int notStartedSubPackages = 0;
        int examReadySubPackages = 0;
        int totalExamAttempts = 0;
        int passedExams = 0;
        int failedExams = 0;

        List<String> expiredSubPackages = new ArrayList<>();
        List<String> expiringSoonSubPackages = new ArrayList<>();
        LocalDate lastActivityDate = null;

        // Fetch all exam data for this user (moved to Exams table)
        List<Exams> userExams = examRepository.findByUserId(userId);
        Map<String, Exams> examsBySubPackageId = userExams.stream()
                .collect(Collectors.toMap(Exams::getSubPackageId, exam -> exam, (e1, e2) -> e1));

        for (UserSubPackage userSubPackage : userSubPackages) {
            // Count by status
            switch (userSubPackage.getStatus()) {
                case "COMPLETED" -> completedSubPackages++;
                case "IN_PROGRESS" -> inProgressSubPackages++;
                case "NOT_STARTED" -> notStartedSubPackages++;
                case "EXAM" -> examReadySubPackages++;
            }

            // Count exam attempts (now from Exams table)
            Exams exam = examsBySubPackageId.get(userSubPackage.getSubPackageId());
            if (exam != null) {
                totalExamAttempts += exam.getExamAttempts();
                if (exam.isExamCompleted()) {
                    if (exam.isExamPassed()) {
                        passedExams++;
                    } else {
                        failedExams++;
                    }
                }
            }

            // Check expiry
            if (isSubPackageExpired(userSubPackage)) {
                expiredSubPackages.add(userSubPackage.getSubPackageId());
            } else if (isSubPackageExpiringSoon(userSubPackage)) {
                expiringSoonSubPackages.add(userSubPackage.getSubPackageId());
            }

            // Track last activity
            if (userSubPackage.getLastSynced() != null) {
                LocalDate activityDate = userSubPackage.getLastSynced().atZone(ZoneId.systemDefault()).toLocalDate();
                if (lastActivityDate == null || activityDate.isAfter(lastActivityDate)) {
                    lastActivityDate = activityDate;
                }
            }
        }

        // Get user details from registration service
        String userName = "Unknown User";
        String userEmail = "unknown@example.com";
        try {
            // This would need to be implemented based on your registration service client
            // UserDetails userDetails = registrationServiceClient.getUserDetails(userId);
            // userName = userDetails.getName();
            // userEmail = userDetails.getEmail();
        } catch (Exception e) {
            log.warn("Could not fetch user details for userId: {}", userId);
        }

        return UserRiskDetailDto.builder()
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .riskCategory(riskCategory)
                .overallProgress(overallProgress)
                .totalSubPackages(totalSubPackages)
                .completedSubPackages(completedSubPackages)
                .inProgressSubPackages(inProgressSubPackages)
                .notStartedSubPackages(notStartedSubPackages)
                .examReadySubPackages(examReadySubPackages)
                .lastActivityDate(lastActivityDate)
                .expiredSubPackages(expiredSubPackages)
                .expiringSoonSubPackages(expiringSoonSubPackages)
                .totalExamAttempts(totalExamAttempts)
                .passedExams(passedExams)
                .failedExams(failedExams)
                .build();
    }

    /**
     * Checks if a sub-package has expired
     */
    private boolean isSubPackageExpired(UserSubPackage userSubPackage) {
        if (userSubPackage.getExpiryDate() == null) {
            return false;
        }
        return userSubPackage.getExpiryDate().isBefore(LocalDate.now());
    }

    /**
     * Checks if a sub-package is expiring soon (within 7 days)
     */
    private boolean isSubPackageExpiringSoon(UserSubPackage userSubPackage) {
        if (userSubPackage.getExpiryDate() == null) {
            return false;
        }
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        return userSubPackage.getExpiryDate().isBefore(sevenDaysFromNow) && 
               userSubPackage.getExpiryDate().isAfter(LocalDate.now());
    }

    /**
     * Creates an empty risk analysis response
     */
    private UserRiskAnalysisResponseDto createEmptyRiskAnalysis(String clientAdminId, String productId) {
        return UserRiskAnalysisResponseDto.builder()
                .safeUsers(0L)
                .lowRisk(0L)
                .averageRisk(0L)
                .highRisk(0L)
                .totalUsers(0L)
                .generatedAt(Instant.now().toString())
                .clientAdminId(clientAdminId)
                .productId(productId)
                .build();
    }
}
