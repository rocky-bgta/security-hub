package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.userRange.UserRangeRequest;
import com.aspire.asat.cms.dto.userRange.UserRangeResponse;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.UserRange;
import com.aspire.asat.cms.repository.UserRangeRepository;
import com.aspire.asat.cms.service.UserRangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRangeServiceImpl implements UserRangeService {

    private final UserRangeRepository userRangeRepository;

    @Override
    @Transactional
    public UserRangeResponse createUserRange(UserRangeRequest request) {
        log.info("Creating user range: {}", request.getRangeName());

        // Validate range
        validateRange(request.getMinUsers(), request.getMaxUsers(), null);

        // Check for overlapping ranges
        List<UserRange> overlappingRanges = userRangeRepository.findOverlappingRanges(
                request.getMinUsers(),
                request.getMaxUsers() != null ? request.getMaxUsers() : Integer.MAX_VALUE
        );
        if (!overlappingRanges.isEmpty()) {
            throw new DuplicateNameException("User range overlaps with existing range(s)");
        }

        // Check for duplicate range name
        if (userRangeRepository.existsByRangeNameAndIsActiveTrue(request.getRangeName())) {
            throw new DuplicateNameException("Range name '" + request.getRangeName() + "' already exists");
        }

        Instant now = Instant.now();
        UserRange userRange = UserRange.builder()
                .id(UUID.randomUUID().toString())
                .rangeName(request.getRangeName())
                .minUsers(request.getMinUsers())
                .maxUsers(request.getMaxUsers())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        UserRange saved = userRangeRepository.save(userRange);
        return mapToResponse(saved);
    }

    @Override
    public List<UserRangeResponse> getAllUserRanges(Boolean isActive) {
        Sort sort = Sort.by(Sort.Direction.ASC, "minUsers");
        List<UserRange> ranges;
        if (isActive == null || isActive) {
            ranges = userRangeRepository.findByIsActiveTrue(sort);
        } else {
            ranges = userRangeRepository.findAll(sort);
        }
        return ranges.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserRangeResponse getUserRangeById(String id) {
        UserRange userRange = userRangeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User range not found with id: " + id));
        return mapToResponse(userRange);
    }

    @Override
    @Transactional
    public UserRangeResponse updateUserRange(String id, UserRangeRequest request) {
        log.info("Updating user range with id: {}", id);

        UserRange existing = userRangeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User range not found with id: " + id));

        // Validate range
        validateRange(request.getMinUsers(), request.getMaxUsers(), id);

        // Check for overlapping ranges (excluding current)
        List<UserRange> overlappingRanges = userRangeRepository.findOverlappingRangesExcludingId(
                request.getMinUsers(),
                request.getMaxUsers() != null ? request.getMaxUsers() : Integer.MAX_VALUE,
                id
        );
        if (!overlappingRanges.isEmpty()) {
            throw new DuplicateNameException("User range overlaps with existing range(s)");
        }

        // Check for duplicate range name (if changed)
        if (!request.getRangeName().equals(existing.getRangeName()) &&
                userRangeRepository.existsByRangeNameAndIsActiveTrue(request.getRangeName())) {
            throw new DuplicateNameException("Range name '" + request.getRangeName() + "' already exists");
        }

        existing.setRangeName(request.getRangeName());
        existing.setMinUsers(request.getMinUsers());
        existing.setMaxUsers(request.getMaxUsers());
        existing.setDescription(request.getDescription());
        existing.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        existing.setUpdatedAt(Instant.now());

        UserRange updated = userRangeRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUserRange(String id) {
        log.info("Deleting user range with id: {}", id);

        UserRange userRange = userRangeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User range not found with id: " + id));

        // Soft delete
        userRange.setIsActive(false);
        userRange.setUpdatedAt(Instant.now());
        userRangeRepository.save(userRange);
    }

    private void validateRange(Integer minUsers, Integer maxUsers, String excludeId) {
        if (minUsers == null) {
            throw new IllegalArgumentException("Minimum users cannot be null");
        }
        if (minUsers < 1) {
            throw new IllegalArgumentException("Minimum users must be at least 1");
        }
        if (maxUsers != null && maxUsers < minUsers) {
            throw new IllegalArgumentException("Maximum users must be greater than or equal to minimum users");
        }
    }

    private UserRangeResponse mapToResponse(UserRange userRange) {
        return UserRangeResponse.builder()
                .id(userRange.getId())
                .rangeName(userRange.getRangeName())
                .minUsers(userRange.getMinUsers())
                .maxUsers(userRange.getMaxUsers())
                .description(userRange.getDescription())
                .createdAt(userRange.getCreatedAt())
                .updatedAt(userRange.getUpdatedAt())
                .isActive(userRange.getIsActive())
                .isDefault(userRange.getIsDefault())
                .build();
    }
}

