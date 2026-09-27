package com.aspire.asat.registration.service.leaderboard.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.leaderboard.LeaderboardRequestDto;
import com.aspire.asat.leaderboard.LeaderboardResponseDto;
import com.aspire.asat.leaderboard.LeaderboardStatusUpdateDto;
import com.aspire.asat.leaderboard.LeaderboardSummaryDto;
import com.aspire.asat.leaderboard.LeaderboardStatus;
import com.aspire.asat.leaderboard.VideoType;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.leaderboard.Leaderboard;
import com.aspire.asat.registration.repository.leaderboard.LeaderboardRepository;
import com.aspire.asat.registration.service.leaderboard.LeaderboardService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Value("${const.leaderboard.max-active-size:2}")
    private int maxActiveLeaderboards;

    @Override
    public LeaderboardResponseDto createLeaderboard(LeaderboardRequestDto request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());
        String username = context.getUsername();

        log.info("Creating leaderboard with title: {} by user: {}", request.getTitle(), username);

        UUID targetClientId = isAdmin ? null : clientId;
        LeaderboardStatus statusToCreate = request.getStatus() != null ? request.getStatus() : LeaderboardStatus.ACTIVE;
        if (statusToCreate == LeaderboardStatus.ACTIVE) {
            long activeCount;
            if (isAdmin) {
                activeCount = leaderboardRepository.countByIsDefaultTrueAndStatus(LeaderboardStatus.ACTIVE);
                if (activeCount >= maxActiveLeaderboards) {
                    throw new DuplicateDataFoundException("Maximum " + maxActiveLeaderboards + " active default leaderboards allowed. Please deactivate one first.");
                }
            } else {
                activeCount = leaderboardRepository.countByClientIdAndStatus(targetClientId, LeaderboardStatus.ACTIVE);
                if (activeCount >= maxActiveLeaderboards) {
                    throw new DuplicateDataFoundException("Maximum " + maxActiveLeaderboards + " active leaderboards allowed. Please deactivate one first.");
                }
            }
        }
        if (isAdmin) {
            if (leaderboardRepository.existsByTitleIgnoreCaseAndIsDefaultTrue(request.getTitle())) {
                throw new DuplicateDataFoundException("Default leaderboard with title " + request.getTitle() + " already exists");
            }
        } else {
            if (targetClientId != null && leaderboardRepository.existsByTitleIgnoreCaseAndClientId(request.getTitle(), targetClientId)) {
                throw new DuplicateDataFoundException("Leaderboard with title " + request.getTitle() + " already exists for this client");
            }
        }

        Leaderboard leaderboard = Leaderboard.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .name(request.getName())
                .designation(request.getDesignation())
                .videoType(request.getVideoType())
                .videoUrl(request.getVideoUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(statusToCreate)
                .createdDate(Instant.now())
                .createdBy(context.getUserId())
                .updatedAt(Instant.now())
                .clientId(targetClientId)
                .isDefault(isAdmin)
                .build();

        Leaderboard savedLeaderboard = leaderboardRepository.save(leaderboard);
        log.info("Successfully created leaderboard with id: {}", savedLeaderboard.getId());

        return mapToResponseDto(savedLeaderboard);
    }

    @Override
    public List<LeaderboardResponseDto> getAllLeaderboards() {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());

        log.info("Fetching all leaderboards for clientId: {}, isAdmin: {}", clientId, isAdmin);

        List<Leaderboard> leaderboards;

        if (isAdmin) {
            // Admin can see all leaderboards (all clients + admin's own defaults)
            leaderboards = leaderboardRepository.findAll();
        } else if(isClient(context.getUserType())){
            // Client sees their own + admin's default leaderboards
            List<Leaderboard> clientLeaderboards = leaderboardRepository.findByClientId(clientId);
            List<Leaderboard> defaultLeaderboards = leaderboardRepository.findByIsDefaultTrue();
            
            leaderboards = new ArrayList<>();
            leaderboards.addAll(clientLeaderboards);
            leaderboards.addAll(defaultLeaderboards);
        } else {
            throw new CustomException("Unauthorized user type: " + context.getUserType());
        }

        return leaderboards.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaderboardResponseDto> getActiveLeaderboardsForWeb() {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID targetClientId = null;
        if(isClient(context.getUserType())){
            targetClientId = parseClientId(context.getUserId());
        }else {
            targetClientId = parseClientId(context.getClientAdminId());
        }

        log.info("Fetching up to 2 active leaderboards for web for clientId: {}", targetClientId);

        List<Leaderboard> result = new ArrayList<>();

        // HIGH PRIORITY: Get client's active leaderboards first
        List<Leaderboard> clientActiveLeaderboards = leaderboardRepository
                .findByClientIdAndStatus(targetClientId, LeaderboardStatus.ACTIVE);

        // Add up to 2 client leaderboards
        int clientCount = Math.min(maxActiveLeaderboards, clientActiveLeaderboards.size());
        for (int i = 0; i < clientCount; i++) {
            result.add(clientActiveLeaderboards.get(i));
        }

        // If we need more (less than 2), get admin defaults
        if (result.size() < maxActiveLeaderboards) {
            List<Leaderboard> adminActiveLeaderboards = leaderboardRepository
                    .findByIsDefaultTrueAndStatus(LeaderboardStatus.ACTIVE);

            int neededCount = maxActiveLeaderboards - result.size();
            int adminCount = Math.min(neededCount, adminActiveLeaderboards.size());

            for (int i = 0; i < adminCount; i++) {
                result.add(adminActiveLeaderboards.get(i));
            }
        }

        log.info("Returning {} active leaderboards for web", result.size());

        return result.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public LeaderboardResponseDto getLeaderboardById(UUID id) {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());

        log.info("Fetching leaderboard by id: {}", id);

        Leaderboard leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard not found with id: " + id));

        // Authorization check: Client can only see their own + admin defaults
        if (!isAdmin) {
            boolean isOwnLeaderboard = leaderboard.getClientId() != null && leaderboard.getClientId().equals(clientId);
            boolean isAdminDefault = leaderboard.getIsDefault() != null && leaderboard.getIsDefault();

            if (!isOwnLeaderboard && !isAdminDefault) {
                throw new ResourceNotFoundException("Leaderboard not found with id: " + id);
            }
        }

        return mapToResponseDto(leaderboard);
    }

    @Override
    public LeaderboardResponseDto updateLeaderboard(UUID id, LeaderboardRequestDto request) {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());
        String username = context.getUsername();

        log.info("Updating leaderboard with id: {} by user: {}", id, username);

        Leaderboard existingLeaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard not found with id: " + id));

        // Authorization check: Client can only update their own leaderboards
        if (!isAdmin) {
            boolean isOwnLeaderboard = existingLeaderboard.getClientId() != null &&
                                      existingLeaderboard.getClientId().equals(clientId);
            if (!isOwnLeaderboard) {
                throw new IllegalArgumentException("You can only update your own leaderboards");
            }
        }

        // Check for duplicate title (not name) - excluding current leaderboard
        if (existingLeaderboard.getIsDefault() != null && existingLeaderboard.getIsDefault()) {
            List<Leaderboard> duplicates = leaderboardRepository.findByIsDefaultTrue().stream()
                    .filter(l -> l.getTitle().equalsIgnoreCase(request.getTitle()) && !l.getId().equals(id))
                    .toList();
            if (!duplicates.isEmpty()) {
                throw new DuplicateDataFoundException("Default leaderboard with title " + request.getTitle() + " already exists");
            }
        } else {
            List<Leaderboard> duplicates = leaderboardRepository.findByClientId(existingLeaderboard.getClientId()).stream()
                    .filter(l -> l.getTitle().equalsIgnoreCase(request.getTitle()) && !l.getId().equals(id))
                    .toList();
            if (!duplicates.isEmpty()) {
                throw new DuplicateDataFoundException("Leaderboard with title " + request.getTitle() + " already exists for this client");
            }
        }

        // Update fields
        existingLeaderboard.setTitle(request.getTitle());
        existingLeaderboard.setName(request.getName());
        existingLeaderboard.setDesignation(request.getDesignation());
        existingLeaderboard.setVideoType(request.getVideoType());
        existingLeaderboard.setVideoUrl(request.getVideoUrl());
        existingLeaderboard.setThumbnailUrl(request.getThumbnailUrl());
        existingLeaderboard.setStatus(request.getStatus() != null ? request.getStatus() : existingLeaderboard.getStatus());
        existingLeaderboard.setUpdatedBy(context.getUserId());
        existingLeaderboard.setUpdatedAt(Instant.now());

        Leaderboard updatedLeaderboard = leaderboardRepository.save(existingLeaderboard);
        log.info("Successfully updated leaderboard with id: {}", updatedLeaderboard.getId());

        return mapToResponseDto(updatedLeaderboard);
    }

    @Override
    public void deleteLeaderboard(UUID id) {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());

        log.info("Deleting leaderboard with id: {}", id);

        Leaderboard leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard not found with id: " + id));

        // Authorization check: Client can only delete their own leaderboards
        if (!isAdmin) {
            boolean isOwnLeaderboard = leaderboard.getClientId() != null &&
                                      leaderboard.getClientId().equals(clientId);
            if (!isOwnLeaderboard) {
                throw new IllegalArgumentException("You can only delete your own leaderboards");
            }
        }

        leaderboardRepository.deleteById(id);
        log.info("Successfully deleted leaderboard with id: {}", id);
    }

    @Override
    public LeaderboardResponseDto updateLeaderboardStatus(LeaderboardStatusUpdateDto request) {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());

        log.info("Updating leaderboard status for id: {} to status: {}", request.getId(), request.getStatus());

        Leaderboard leaderboard = leaderboardRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard not found with id: " + request.getId()));

        // Authorization check: Client can only update status of their own leaderboards
        if (!isAdmin) {
            boolean isOwnLeaderboard = leaderboard.getClientId() != null &&
                                      leaderboard.getClientId().equals(clientId);
            if (!isOwnLeaderboard) {
                throw new IllegalArgumentException("You can only update status of your own leaderboards");
            }
        }

        // If changing status to ACTIVE, validate the 2 active leaderboard limit
        if (request.getStatus() == LeaderboardStatus.ACTIVE && leaderboard.getStatus() != LeaderboardStatus.ACTIVE) {
            long activeCount;
            if (leaderboard.getIsDefault() != null && leaderboard.getIsDefault()) {
                // For admin defaults, check count of active default leaderboards
                activeCount = leaderboardRepository.countByIsDefaultTrueAndStatus(LeaderboardStatus.ACTIVE);
                if (activeCount >= maxActiveLeaderboards) {
                    throw new IllegalArgumentException("Cannot activate more than " + maxActiveLeaderboards + " default leaderboards. Currently " + activeCount + " active default leaderboards exist.");
                }
            } else {
                // For client-specific, check count of active leaderboards for this client
                activeCount = leaderboardRepository.countByClientIdAndStatus(leaderboard.getClientId(), LeaderboardStatus.ACTIVE);
                if (activeCount >= maxActiveLeaderboards) {
                    throw new IllegalArgumentException("Cannot activate more than " + maxActiveLeaderboards + " leaderboards. You currently have " + activeCount + " active leaderboards.");
                }
            }
        }

        leaderboard.setStatus(request.getStatus());
        leaderboard.setUpdatedAt(Instant.now());

        Leaderboard updatedLeaderboard = leaderboardRepository.save(leaderboard);
        log.info("Successfully updated leaderboard status for id: {}", updatedLeaderboard.getId());

        return mapToResponseDto(updatedLeaderboard);
    }

    @Override
    public LeaderboardSummaryDto getLeaderboardSummary() {
        // Get current user context from service
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UUID clientId = parseClientId(context.getUserId());
        boolean isAdmin = isAdmin(context.getUserType());

        log.info("Fetching leaderboard summary for clientId: {}, isAdmin: {}", clientId, isAdmin);

        long totalLeaders;
        long activeLeaders;
        long inactiveLeaders;
        long uploadedVideos;

        if (isAdmin) {
            // Admin sees everything
            totalLeaders = leaderboardRepository.count();
            activeLeaders = leaderboardRepository.countByStatus(LeaderboardStatus.ACTIVE);
            inactiveLeaders = leaderboardRepository.countByStatus(LeaderboardStatus.INACTIVE);
            uploadedVideos = leaderboardRepository.countByVideoType(VideoType.UPLOAD_FILE);
        } else {
            // Client sees their own + default
            long clientTotal = leaderboardRepository.countByClientId(clientId);
            long defaultTotal = leaderboardRepository.countByIsDefaultTrue();
            totalLeaders = clientTotal + defaultTotal;

            long clientActive = leaderboardRepository.countByClientIdAndStatus(clientId, LeaderboardStatus.ACTIVE);
            long defaultActive = leaderboardRepository.findByIsDefaultTrue().stream()
                    .filter(l -> l.getStatus() == LeaderboardStatus.ACTIVE)
                    .count();
            activeLeaders = clientActive + defaultActive;

            long clientInactive = leaderboardRepository.countByClientIdAndStatus(clientId, LeaderboardStatus.INACTIVE);
            long defaultInactive = leaderboardRepository.findByIsDefaultTrue().stream()
                    .filter(l -> l.getStatus() == LeaderboardStatus.INACTIVE)
                    .count();
            inactiveLeaders = clientInactive + defaultInactive;

            long clientUploaded = leaderboardRepository.countByClientIdAndVideoType(clientId, VideoType.UPLOAD_FILE);
            long defaultUploaded = leaderboardRepository.findByIsDefaultTrue().stream()
                    .filter(l -> l.getVideoType() == VideoType.UPLOAD_FILE)
                    .count();
            uploadedVideos = clientUploaded + defaultUploaded;
        }

        return LeaderboardSummaryDto.builder()
                .totalLeaders(totalLeaders)
                .activeLeaders(activeLeaders)
                .inactiveLeaders(inactiveLeaders)
                .uploadedVideos(uploadedVideos)
                .build();
    }

    // Private helper methods moved from controller
    private UUID parseClientId(String userId) {
        try {
            return userId != null && !userId.equals("system") ? UUID.fromString(userId) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid client admin ID format: {}", userId);
            return null;
        }
    }

    private boolean isAdmin(String userType) {
        return userType != null && (userType.equalsIgnoreCase("SUPER_ADMIN") || userType.equalsIgnoreCase("ASPIRE_ADMIN") || userType.equalsIgnoreCase("SYSTEM_USER"));
    }

    private boolean isClient(String userType) {
        return userType != null && (userType.equalsIgnoreCase("CLIENT_ADMIN") || userType.equalsIgnoreCase("CLIENT"));
    }

    private LeaderboardResponseDto mapToResponseDto(Leaderboard leaderboard) {
        return LeaderboardResponseDto.builder()
                .id(leaderboard.getId())
                .title(leaderboard.getTitle())
                .name(leaderboard.getName())
                .designation(leaderboard.getDesignation())
                .videoType(leaderboard.getVideoType())
                .videoUrl(leaderboard.getVideoUrl())
                .thumbnailUrl(leaderboard.getThumbnailUrl())
                .status(leaderboard.getStatus())
                .createdDate(leaderboard.getCreatedDate())
                .createdBy(leaderboard.getCreatedBy())
                .updatedBy(leaderboard.getUpdatedBy())
                .updatedAt(leaderboard.getUpdatedAt())
                .clientId(leaderboard.getClientId())
                .clientName(null) // TODO: Fetch client name if needed
                .isDefault(leaderboard.getIsDefault())
                .build();
    }
}
