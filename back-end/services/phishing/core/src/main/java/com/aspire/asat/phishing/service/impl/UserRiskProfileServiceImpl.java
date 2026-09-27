package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.RegistrationRiskGroupSyncService;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of UserRiskProfileService. Creates or updates a profile by clientId + userId.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRiskProfileServiceImpl implements UserRiskProfileService {

    private final UserRiskProfileRepository userRiskProfileRepository;
    private final RegistrationRiskGroupSyncService registrationRiskGroupSyncService;
    @Value("${risk-score.training-weight}")
    private Double trainingWeight;

    @Value("${risk-score.phishing-weight}")
    private Double phishingWeight;

    @Override
    public UserRiskSummaryDto save(UserRiskProfileSaveRequestDto request) {
        Optional<UserRiskProfile> existingOpt = userRiskProfileRepository
                .findByClientIdAndUserId(request.getClientId(), request.getUserId());
        boolean isNew = existingOpt.isEmpty();
        RiskLevel previousLevel = !isNew ? existingOpt.get().getRiskLevel() : null;

        UserRiskProfile profile = existingOpt.orElseGet(() -> {
            UserRiskProfile p = new UserRiskProfile();
            p.setId(request.getId() != null ? request.getId() : UUID.randomUUID().toString());
            p.setEmail(request.getEmail());
            p.setFirstName(request.getFirstName());
            p.setLastName(request.getLastName());
            p.setDepartment(request.getDepartment());
            p.setClientId(request.getClientId());
            p.setUserId(request.getUserId());
            p.setRiskLevel(RiskLevel.HIGH);
            p.setRiskScore(0.0);
            p.setPhishingRiskScore(0.0);
            p.setTrainingRiskScore(0.0);
            p.setCampaignsTargeted(0);
            p.setEmailsReceived(0);
            p.setEmailsOpened(0);
            p.setLinksClicked(0);
            p.setDataSubmissions(0);
            p.setEmailsReported(0);
            p.setBreachesInvolved(0);
            p.setIsPhishingEnabled(request.getIsPhishingEnabled() != null ? request.getIsPhishingEnabled() : false);
            return p;
        });

        // Set/update module flags by caller context
        if (Boolean.TRUE.equals(request.getFromTrainingContext())) {
            if (isNew) {
                profile.setIsTrainingEnabled(true);
            } else {
                if (request.getIsTrainingEnabled() != null) {
                    profile.setIsTrainingEnabled(request.getIsTrainingEnabled());
                } else {
                    profile.setIsTrainingEnabled(true);
                }
            }
        }
        if (Boolean.TRUE.equals(request.getFromPhishingContext())) {
            profile.setIsPhishingEnabled(true);
        }

        Boolean isTrainingEnabled = profile.getIsTrainingEnabled() != null ? profile.getIsTrainingEnabled() : false ;
        Boolean isPhishingEnabled = profile.getIsPhishingEnabled() != null ? profile.getIsPhishingEnabled() : false;
        double training = profile.getTrainingRiskScore() != null ? profile.getTrainingRiskScore() : 0.0;
        double phishing = profile.getPhishingRiskScore() != null ? profile.getPhishingRiskScore() : 0.0;
        double riskScore = RiskScoreUtils.computeOverallRiskScore(training, phishing,
                isTrainingEnabled, isPhishingEnabled, trainingWeight, phishingWeight);

        profile.setRiskScore(riskScore);
        profile.setRiskLevelFromScore();
        profile.setUpdatedAt(Instant.now());
        UserRiskProfile saved = userRiskProfileRepository.save(profile);
        log.debug("Saved UserRiskProfile for clientId={}, userId={}", saved.getClientId(), saved.getUserId());
        registrationRiskGroupSyncService.syncIfNeeded(saved.getUserId(), previousLevel, saved.getRiskLevel());
        return toUserRiskSummaryDto(saved);
    }



    @Override
    public void createRiskProfile(UserRiskProfileSaveRequestDto request) {
        UserRiskProfile profile = new UserRiskProfile();
        profile.setId(request.getId() != null ? request.getId() : UUID.randomUUID().toString());
        profile.setClientId(request.getClientId());
        profile.setUserId(request.getUserId());
        profile.setRiskLevel(RiskLevel.HIGH);
        profile.setRiskScore(0.0);
        profile.setPhishingRiskScore(0.0);
        profile.setTrainingRiskScore(0.0);
        profile.setCampaignsTargeted(0);
        profile.setEmailsReceived(0);
        profile.setEmailsOpened(0);
        profile.setLinksClicked(0);
        profile.setDataSubmissions(0);
        profile.setEmailsReported(0);
        profile.setBreachesInvolved(0);

        if (Boolean.TRUE.equals(request.getFromPhishingContext())) {
            profile.setIsPhishingEnabled(true);
        }

        applyRequestToProfile(request, profile);
        if (request.getRiskScore() == null) {
            Boolean isTrainingEnabled = profile.getIsTrainingEnabled() != null ? profile.getIsTrainingEnabled() : false;
            Boolean isPhishingEnabled = profile.getIsPhishingEnabled() != null ? profile.getIsPhishingEnabled() : false;
            double training = profile.getTrainingRiskScore() != null ? profile.getTrainingRiskScore() : 0.0;
            double phishing = profile.getPhishingRiskScore() != null ? profile.getPhishingRiskScore() : 0.0;
            double riskScore = RiskScoreUtils.computeOverallRiskScore(
                    training, phishing, isTrainingEnabled, isPhishingEnabled, trainingWeight, phishingWeight);
            profile.setRiskScore(riskScore);
        }
        profile.setRiskLevelFromScore();
        profile.setUpdatedAt(Instant.now());
        userRiskProfileRepository.save(profile);
        log.debug("Created UserRiskProfile for clientId={}, userId={}", profile.getClientId(), profile.getUserId());
        registrationRiskGroupSyncService.syncIfNeeded(profile.getUserId(), null, profile.getRiskLevel());
    }

    @Override
    public UserRiskSummaryDto getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return userRiskProfileRepository.findById(id)
                .map(this::toUserRiskSummaryDto)
                .orElse(null);
    }

    @Override
    public UserRiskSummaryDto updateById(String id, UserRiskProfileSaveRequestDto request) {
        if (id == null || id.isBlank() || request == null) {
            return null;
        }
        Optional<UserRiskProfile> existing = userRiskProfileRepository.findById(id);
        if (existing.isEmpty()) {
            return null;
        }
        UserRiskProfile profile = existing.get();
        RiskLevel previousLevel = profile.getRiskLevel();
        applyRequestToProfile(request, profile);
        if (request.getRiskScore() == null) {
            Boolean isTrainingEnabled = profile.getIsTrainingEnabled() != null ? profile.getIsTrainingEnabled() : false;
            Boolean isPhishingEnabled = profile.getIsPhishingEnabled() != null ? profile.getIsPhishingEnabled() : false;
            double training = profile.getTrainingRiskScore() != null ? profile.getTrainingRiskScore() : 0.0;
            double phishing = profile.getPhishingRiskScore() != null ? profile.getPhishingRiskScore() : 0.0;
            double riskScore = RiskScoreUtils.computeOverallRiskScore(
                    training, phishing, isTrainingEnabled, isPhishingEnabled, trainingWeight, phishingWeight);
            profile.setRiskScore(riskScore);
        }
        profile.setRiskLevelFromScore();
        profile.setUpdatedAt(Instant.now());
        UserRiskProfile saved = userRiskProfileRepository.save(profile);
        log.debug("Updated UserRiskProfile id={}", id);
        registrationRiskGroupSyncService.syncIfNeeded(saved.getUserId(), previousLevel, saved.getRiskLevel());
        return toUserRiskSummaryDto(saved);
    }

    @Override
    public PageResult<UserRiskSummaryDto> getList(String clientAdminId, String department, String search,
                                                 int offset, int pageSize, String sortBy, String sortOrder) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return new PageResult<>(List.of(), 0L, offset, pageSize);
        }
        String clientId = clientAdminId;
        Sort sort = buildSort(sortBy, sortOrder);
        int page = offset;
        Pageable pageable = PageRequest.of(page, pageSize > 0 ? pageSize : 20, sort);

        Page<UserRiskProfile> pageResult = userRiskProfileRepository.findWithFilters(clientId, department, search, pageable);

        List<UserRiskSummaryDto> items = pageResult.getContent().stream()
                .map(this::toUserRiskSummaryDto)
                .collect(Collectors.toList());
        return new PageResult<>(items, pageResult.getTotalElements(), offset, pageSize);
    }

    @Override
    public byte[] exportCsv(String clientAdminId, String department, String search) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return new byte[0];
        }
        String clientId = clientAdminId;
        Sort sort = Sort.by(Sort.Direction.DESC, "riskScore");
        List<UserRiskSummaryDto> pageItems = userRiskProfileRepository.findListWithFilters(clientId, department, search, sort).stream()
                .map(this::toUserRiskSummaryDto)
                .toList();

        StringBuilder csv = new StringBuilder();
        csv.append("UserId,Email,FirstName,LastName,FullName,Department,RiskLevel,RiskScore,CampaignsTargeted,EmailsReceived,EmailsOpened,EmailsClicked,DataSubmissions,EmailsReported,BreachesInvolved,RepeatOffender,LastActivityAt,LastClickedAt\n");
        for (UserRiskSummaryDto item : pageItems) {
            csv.append(csvValue(item.getUserId())).append(",");
            csv.append(csvValue(item.getEmail())).append(",");
            csv.append(csvValue(item.getFirstName())).append(",");
            csv.append(csvValue(item.getLastName())).append(",");
            csv.append(csvValue(item.getFullName())).append(",");
            csv.append(csvValue(item.getDepartment())).append(",");
            csv.append(csvValue(item.getRiskLevel() != null ? item.getRiskLevel().name() : "")).append(",");
            csv.append(csvValue(item.getRiskScore())).append(",");
            csv.append(csvValue(item.getCampaignsTargeted())).append(",");
            csv.append(csvValue(item.getEmailsReceived())).append(",");
            csv.append(csvValue(item.getEmailsOpened())).append(",");
            csv.append(csvValue(item.getEmailsClicked())).append(",");
            csv.append(csvValue(item.getDataSubmissions())).append(",");
            csv.append(csvValue(item.getEmailsReported())).append(",");
            csv.append(csvValue(item.getBreachesInvolved())).append(",");
            csv.append(csvValue(item.isRepeatOffender())).append(",");
            csv.append(csvValue(item.getLastActivityAt())).append(",");
            csv.append(csvValue(item.getLastClickedAt())).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getExistingUserIds(String clientId, List<String> userIds) {
        if (clientId == null || clientId.isBlank() || userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<UserRiskProfile> profiles = userRiskProfileRepository.findByClientIdAndUserIdIn(clientId, userIds);
        return profiles.stream()
                .map(UserRiskProfile::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String field = (sortBy != null && !sortBy.isBlank()) ? sortBy : "riskScore";
        boolean desc = "asc".equalsIgnoreCase(sortOrder) ? false : true;
        return Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, mapSortField(field));
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "email" -> "email";
            case "firstname", "firstName" -> "firstName";
            case "lastname", "lastName" -> "lastName";
            case "department" -> "department";
            case "riskscore", "riskScore" -> "riskScore";
            case "risklevel", "riskLevel" -> "riskLevel";
            case "updatedat", "updatedAt" -> "updatedAt";
            default -> "riskScore";
        };
    }

    private void applyRequestToProfile(UserRiskProfileSaveRequestDto request, UserRiskProfile profile) {
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
        if (request.getRiskLevel() != null) profile.setRiskLevel(request.getRiskLevel());
        if (request.getRiskScore() != null) profile.setRiskScore(request.getRiskScore());
        if (request.getPhishingRiskScore() != null) profile.setPhishingRiskScore(request.getPhishingRiskScore());
        if (request.getTrainingRiskScore() != null) profile.setTrainingRiskScore(request.getTrainingRiskScore());
        if (request.getIsTrainingEnabled() != null) profile.setIsTrainingEnabled(request.getIsTrainingEnabled());
        if (request.getIsPhishingEnabled() != null) profile.setIsPhishingEnabled(request.getIsPhishingEnabled());
        if (request.getCampaignsTargeted() != null) profile.setCampaignsTargeted(request.getCampaignsTargeted());
        if (request.getEmailsReceived() != null) profile.setEmailsReceived(request.getEmailsReceived());
        if (request.getEmailsOpened() != null) profile.setEmailsOpened(request.getEmailsOpened());
        if (request.getLinksClicked() != null) profile.setLinksClicked(request.getLinksClicked());
        if (request.getDataSubmissions() != null) profile.setDataSubmissions(request.getDataSubmissions());
        if (request.getEmailsReported() != null) profile.setEmailsReported(request.getEmailsReported());
        if (request.getBreachesInvolved() != null) profile.setBreachesInvolved(request.getBreachesInvolved());
        if (request.getLastActivityAt() != null) profile.setLastActivityAt(request.getLastActivityAt());
        if (request.getLastClickedAt() != null) profile.setLastClickedAt(request.getLastClickedAt());
        if (request.getLastReportedAt() != null) profile.setLastReportedAt(request.getLastReportedAt());
    }

    private UserRiskSummaryDto toUserRiskSummaryDto(UserRiskProfile profile) {
        String fullName = "";
        if (profile.getFirstName() != null) fullName += profile.getFirstName();
        if (profile.getLastName() != null) fullName += " " + profile.getLastName();

        return UserRiskSummaryDto.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(fullName.trim())
                .department(profile.getDepartment())
                .riskLevel(profile.getRiskLevel())
                .riskScore(RiskScoreUtils.roundToTwoDecimals(profile.getRiskScore()))
                .campaignsTargeted(profile.getCampaignsTargeted())
                .emailsReceived(profile.getEmailsReceived())
                .emailsOpened(profile.getEmailsOpened())
                .emailsClicked(profile.getLinksClicked())
                .dataSubmissions(profile.getDataSubmissions())
                .emailsReported(profile.getEmailsReported())
                .breachesInvolved(profile.getBreachesInvolved())
                .isRepeatOffender(profile.isRepeatOffender())
                .lastActivityAt(profile.getLastActivityAt())
                .lastClickedAt(profile.getLastClickedAt())
                .build();
    }

}
