package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.userLicence.UserLicenceResponseDto;
import com.aspire.asat.cms.dto.userLicence.BulkUserLicenceRequestDto;
import com.aspire.asat.cms.model.UserLicence;
import com.aspire.asat.cms.repository.UserLicenceRepository;
import com.aspire.asat.cms.service.UserLicenceService;
import com.aspire.asat.cms.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLicenceServiceImpl implements UserLicenceService {

    private final UserLicenceRepository userLicenceRepository;

    @Override
    public List<UserLicenceResponseDto> createBulkUserLicences(BulkUserLicenceRequestDto requestDto) {
        log.info("Creating {} user licences in bulk", requestDto.getUserLicenceData().size());

        List<UserLicence> userLicences = new ArrayList<>();
        
        for (BulkUserLicenceRequestDto.UserLicenceData licenceData : requestDto.getUserLicenceData()) {
            // Check if licence already exists for this user and package combination
            if (userLicenceRepository.existsByUserIdAndPackageId(licenceData.getUserId(), licenceData.getPackageId())) {
                log.warn("User licence already exists for userId: {} and packageId: {}", 
                        licenceData.getUserId(), licenceData.getPackageId());
                continue; // Skip this licence
            }

            UserLicence userLicence = UserLicence.builder()
                    .id(CommonUtil.generateUUID())
                    .userId(licenceData.getUserId())
                    .clientAdminId(licenceData.getClientAdminId())
                    .productId(licenceData.getProductId())
                    .packageId(licenceData.getPackageId())
                    .subPackageId(licenceData.getSubPackageId())
                    .licenceStatus(licenceData.getLicenceStatus())
                    .issueDate(licenceData.getIssueDate())
                    .expireDate(licenceData.getExpireDate())
                    .build();

            userLicences.add(userLicence);
        }

        if (userLicences.isEmpty()) {
            log.warn("No new user licences to create - all already exist");
            return List.of();
        }

        List<UserLicence> savedUserLicences = userLicenceRepository.saveAll(userLicences);
        log.info("Successfully created {} user licences in bulk", savedUserLicences.size());

        return savedUserLicences.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert UserLicence entity to UserLicenceResponseDto
     */
    private UserLicenceResponseDto convertToResponseDto(UserLicence userLicence) {
        return UserLicenceResponseDto.builder()
                .id(userLicence.getId())
                .userId(userLicence.getUserId())
                .clientAdminId(userLicence.getClientAdminId())
                .productId(userLicence.getProductId())
                .packageId(userLicence.getPackageId())
                .subPackageId(userLicence.getSubPackageId())
                .licenceStatus(userLicence.getLicenceStatus())
                .issueDate(userLicence.getIssueDate())
                .expireDate(userLicence.getExpireDate())
                .build();
    }
}