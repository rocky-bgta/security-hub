package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.activitylog.ActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogResponseDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.util.UserTypeInferenceUtil;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.ActivityLog;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.repository.ActivityLogRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Async
    public ActivityLog logActivity(ActivityLog activityLog) {
        if (activityLog.getCreatedAt() == null) {
            activityLog.setCreatedAt(Instant.now());
        }
        log.info("Logging activity: {} for user: {}", activityLog.getActivityType(), activityLog.getUserId());
        return activityLogRepository.save(activityLog);
    }

    @Override
    @Async
    public ActivityLog logActivity(String userId, UserType userType,
                                   ActivityType activityType, ActivityStatus activityStatus, String description, String ipAddress) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .userType(userType)
                .activityType(activityType)
                .activityStatus(activityStatus)
                .description(description)
                .createdAt(Instant.now())
                .ipAddress(ipAddress)
                .build();
        return activityLogRepository.save(activityLog);
    }

    @Override
    @Async
    public ActivityLog logActivity(String userId, String email, String username, String fullName, UserType userType,
                                   ActivityType activityType, ActivityStatus activityStatus, String description, String ipAddress) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .fullName(fullName)
                .userType(userType)
                .activityType(activityType)
                .activityStatus(activityStatus)
                .description(description)
                .createdAt(Instant.now())
                .ipAddress(ipAddress)
                .build();
        return activityLogRepository.save(activityLog);
    }

    @Override
    @Async
    public ActivityLog logActivity(String userId, UserType userType,
                                   ActivityType activityType, ActivityStatus activityStatus,  String description, String oldValue, String newValue, String ipAddress) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .userType(userType)
                .activityType(activityType)
                .activityStatus(activityStatus)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(Instant.now())
                .ipAddress(ipAddress)
                .build();
        return activityLogRepository.save(activityLog);
    }

    @Override
    @Async
    public ActivityLog logActivity(String userId, String email, String username, String fullName, UserType userType,
                                   ActivityType activityType, ActivityStatus activityStatus, String description, String oldValue, String newValue, String ipAddress) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .fullName(fullName)
                .userType(userType)
                .activityType(activityType)
                .activityStatus(activityStatus)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(Instant.now())
                .ipAddress(ipAddress)
                .build();
        return activityLogRepository.save(activityLog);
    }

    @Override
    public Page<ActivityLog> getActivitiesByUserId(String userId, Pageable pageable) {
        return activityLogRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<ActivityLog> getActivitiesByUserType(UserType userType, Pageable pageable) {
        return activityLogRepository.findByUserType(userType, pageable);
    }

    @Override
    public Page<ActivityLog> getActivitiesByActivityType(ActivityType activityType, Pageable pageable) {
        return activityLogRepository.findByActivityType(activityType, pageable);
    }

    @Override
    public List<ActivityLog> getActivitiesByDateRange(Instant startDate, Instant endDate) {
        return activityLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public Page<ActivityLog> getAllActivities(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }

    @Override
    public Page<ActivityLog> getClientAdminActivityLogs(
            String clientAdminId,
            String search,
            ActivityType activityType,
            String status,
            String ipAddress,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        log.info("Fetching activity logs for clientAdminId: {} with filters", clientAdminId);

        List<Criteria> criteriaList = new ArrayList<>();

        // Filter by user type CLIENT_ADMIN
        criteriaList.add(Criteria.where("userType").is(UserType.CLIENT_ADMIN));

        if (clientAdminId != null && !clientAdminId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(clientAdminId));
        }

        if (search != null && !search.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("description").regex(search, "i"),
                    Criteria.where("userId").regex(search, "i")
            ));
        }

        if (activityType != null) {
            criteriaList.add(Criteria.where("activityType").is(activityType));
        }

        if (status != null && !status.isBlank()) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (ipAddress != null && !ipAddress.isBlank()) {
            criteriaList.add(Criteria.where("ipAddress").is(ipAddress));
        }

        if (startDate != null && endDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        } else if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        Query countQuery = new Query(criteria);

        List<ActivityLog> logs = mongoTemplate.find(query, ActivityLog.class);
        long total = mongoTemplate.count(countQuery, ActivityLog.class);

        return new PageImpl<>(logs, pageable, total);
    }

    @Override
    public ActivityLog getActivityLogById(String activityId) {
        log.info("Fetching activity log by ID: {}", activityId);
        return activityLogRepository.findById(activityId)
                .orElseThrow(() -> new RegistrationServiceException("Activity log not found with ID: " + activityId));
    }

    @Override
    public ClientAdminActivityLogResponseDto getClientAdminActivityLogs(ClientAdminActivityLogRequestDto requestDto) {
        log.info("Fetching client admin activity logs with filters");

        int page = requestDto.getOffset() != null ? requestDto.getOffset() : 0;
        int size = requestDto.getPageSize() != null ? requestDto.getPageSize() : 10;
        String sortBy = requestDto.getSortBy() != null ? requestDto.getSortBy() : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(requestDto.getOrder())
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ActivityLog> activityLogs = getClientAdminActivityLogs(
                requestDto.getClientAdminId(),
                requestDto.getSearch(),
                requestDto.getActivityType(),
                requestDto.getStatus(),
                requestDto.getIpAddress(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                pageable
        );

        List<ClientAdminActivityLogDto> activityLogDtos = activityLogs.getContent().stream()
                .map(this::mapToClientAdminActivityLogDto)
                .toList();

        return ClientAdminActivityLogResponseDto.builder()
                .activityLogs(activityLogDtos)
                .totalElements(activityLogs.getTotalElements())
                .totalPages(activityLogs.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @Override
    public ClientAdminActivityLogDto getClientAdminActivityLogById(String activityId) {
        log.info("Fetching client admin activity log by ID: {}", activityId);
        ActivityLog activityLog = getActivityLogById(activityId);
        return mapToClientAdminActivityLogDto(activityLog);
    }

    private ClientAdminActivityLogDto mapToClientAdminActivityLogDto(ActivityLog activityLog) {
        String clientAdminName = "";
        String clientAdminEmail = "";
        String organizationName = "";

        if (activityLog.getUserId() != null) {
            Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(activityLog.getUserId());
            if (clientAdminOpt.isPresent()) {
                ClientAdmin clientAdmin = clientAdminOpt.get();
                clientAdminEmail = clientAdmin.getEmail();
                organizationName = clientAdmin.getOrganizationName();
                // Use organization name as the display name since ClientAdmin doesn't have firstName/lastName
                clientAdminName = organizationName != null ? organizationName : clientAdminEmail;
            }
        }

        return ClientAdminActivityLogDto.builder()
                .activityId(activityLog.getId())
                .clientAdminId(activityLog.getUserId())
                .clientAdminName(clientAdminName)
                .clientAdminEmail(clientAdminEmail)
                .organizationName(organizationName)
                .actionPerformed(activityLog.getActivityType())
                .actionDescription(getActionDescription(activityLog.getActivityType()))
                .timestamp(activityLog.getCreatedAt())
                .status(activityLog.getActivityStatus() != null ? activityLog.getActivityStatus().name() : "SUCCESS")
                .ipAddress(activityLog.getIpAddress())
                .details(activityLog.getDescription())
                .oldValue(activityLog.getOldValue())
                .newValue(activityLog.getNewValue())
                .build();
    }

    private String getActionDescription(ActivityType activityType) {
        if (activityType == null) return "Unknown Action";

        return switch (activityType) {
            case CLIENT_CREATED -> "Client Created";
            case CLIENT_UPDATED -> "Profile Updated";
            case LICENSE_ALLOCATED -> "License Allocated";
            case PASSWORD_CHANGED -> "Password Changed";
            case PASSWORD_RESET -> "Password Reset";
            default -> activityType.name().replace("_", " ");
        };
    }

    @Override
    public ActivityLogResponseDto getActivityLogs(ActivityLogRequestDto requestDto, CurrentUserContext currentUserContext) {
        String currentUserId = currentUserContext.getUserId();
        String currentUserTypeStr = currentUserContext.getUserType();
        String currentMspId = currentUserContext.getMspId();
        String currentClientAdminId = currentUserContext.getClientAdminId();

        log.info("Fetching activity logs for user: {} with userType: {}", currentUserId, currentUserTypeStr);

        // Resolve current user type (handle "Aspire Admin", "ASPIRE_ADMIN", etc.)
        UserType resolvedUserType = UserTypeInferenceUtil.inferUserTypeFromString(currentUserTypeStr);
        if (resolvedUserType == null) {
            resolvedUserType = UserTypeInferenceUtil.inferUserTypeFromContext(currentUserId, currentMspId, currentClientAdminId);
        }

        // Set up pagination
        int page = requestDto.getOffset() != null ? requestDto.getOffset() : 0;
        int size = requestDto.getPageSize() != null ? requestDto.getPageSize() : 10;
        String sortBy = requestDto.getSortBy() != null ? requestDto.getSortBy() : "createdAt";
        Sort.Direction direction = (requestDto.getOrder() != null && "asc".equalsIgnoreCase(requestDto.getOrder()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        List<Criteria> criteriaList = new ArrayList<>();

        // Role-based scope: userType filter + fixed scope (userId / clientAdminId / mspId)
        if (resolvedUserType == null) {
            // Unknown role: no access
            criteriaList.add(Criteria.where("id").is("__no_match__"));
        } else {
            switch (resolvedUserType) {
                case USER -> {
                    criteriaList.add(Criteria.where("userType").is(UserType.USER));
                    criteriaList.add(Criteria.where("userId").is(currentUserId));
                }
                case CLIENT_ADMIN, CLIENT -> {
                    criteriaList.add(Criteria.where("userType").in(UserType.USER, UserType.CLIENT_ADMIN));
                    criteriaList.add(Criteria.where("clientAdminId").is(currentUserId));
                }
                case MSP -> {
                    // Default MSP scope; overridden below when clientAdminId filter is provided
                    if (requestDto.getClientAdminId() == null || requestDto.getClientAdminId().isBlank()) {
                        criteriaList.add(Criteria.where("userType").in(UserType.MSP, UserType.USER, UserType.CLIENT_ADMIN));
                        if (currentMspId != null && !currentMspId.isBlank()) {
                            criteriaList.add(Criteria.where("mspId").is(currentMspId));
                        } else {
                            criteriaList.add(Criteria.where("mspId").is("__no_match__"));
                        }
                    }
                }
                case ASPIRE_ADMIN, SUPER_ADMIN, SYSTEM_USER -> {
                    // No userType or scope restriction; list includes all logs (USER, CLIENT_ADMIN, MSP, ASPIRE_ADMIN, SYSTEM_USER, etc.)
                }
                default -> criteriaList.add(Criteria.where("id").is("__no_match__"));
            }
        }

        // Common filters (all roles): search, countryId, activityType, activityStatus, startDate, endDate
        if (requestDto.getSearch() != null && !requestDto.getSearch().isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("description").regex(escapeRegex(requestDto.getSearch()), "i"),
                    Criteria.where("username").regex(escapeRegex(requestDto.getSearch()), "i"),
                    Criteria.where("email").regex(escapeRegex(requestDto.getSearch()), "i"),
                    Criteria.where("fullName").regex(escapeRegex(requestDto.getSearch()), "i")
            ));
        }
        if (requestDto.getCountryId() != null && !requestDto.getCountryId().isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(requestDto.getCountryId()));
        }
        if (requestDto.getActivityType() != null) {
            criteriaList.add(Criteria.where("activityType").is(requestDto.getActivityType()));
        }
        if (requestDto.getActivityStatus() != null) {
            criteriaList.add(Criteria.where("activityStatus").is(requestDto.getActivityStatus()));
        }
        if (requestDto.getStartDate() != null && requestDto.getEndDate() != null) {
            criteriaList.add(Criteria.where("createdAt").gte(requestDto.getStartDate()).lte(requestDto.getEndDate()));
        } else if (requestDto.getStartDate() != null) {
            criteriaList.add(Criteria.where("createdAt").gte(requestDto.getStartDate()));
        } else if (requestDto.getEndDate() != null) {
            criteriaList.add(Criteria.where("createdAt").lte(requestDto.getEndDate()));
        }

        // Role-specific optional filters
        if (resolvedUserType == UserType.MSP) {
            // MSP can filter by clientAdminId (within their msp scope)
            if (requestDto.getClientAdminId() != null && !requestDto.getClientAdminId().isBlank()) {
                if ("ALL".equalsIgnoreCase(requestDto.getClientAdminId())) {
                    // All CLIENT_ADMIN activity logs under this MSP
                    criteriaList.add(Criteria.where("userType").is(UserType.CLIENT_ADMIN));
                    criteriaList.add(Criteria.where("mspId").is(currentUserId));
                } else {
                    // Specific client admin under this MSP
                    criteriaList.add(Criteria.where("userType").is(UserType.CLIENT_ADMIN));
                    criteriaList.add(Criteria.where("clientAdminId").is(requestDto.getClientAdminId()));
                    criteriaList.add(Criteria.where("mspId").is(currentUserId));
                }
            }
        } else if (resolvedUserType == UserType.ASPIRE_ADMIN || resolvedUserType == UserType.SUPER_ADMIN
                || resolvedUserType == UserType.SYSTEM_USER) {
            // Aspire Admin / System User can filter by mspId, clientAdminId, aspireAdminId
            if (requestDto.getMspId() != null && !requestDto.getMspId().isBlank()) {
                if ("ALL".equalsIgnoreCase(requestDto.getMspId())) {
                    // All MSP activity logs (skip when also filtering by clientAdminId)
                    if (requestDto.getClientAdminId() == null || requestDto.getClientAdminId().isBlank()) {
                        criteriaList.add(Criteria.where("userType").is(UserType.MSP));
                    }
                } else {
                    criteriaList.add(Criteria.where("mspId").is(requestDto.getMspId()));
                }
            }
            if (requestDto.getClientAdminId() != null && !requestDto.getClientAdminId().isBlank()) {
                if ("ALL".equalsIgnoreCase(requestDto.getClientAdminId())) {
                    // All CLIENT_ADMIN activity logs under this MSP
                    criteriaList.add(Criteria.where("userType").is(UserType.CLIENT_ADMIN));
                } else {
                    // Specific client admin
                    criteriaList.add(Criteria.where("userType").is(UserType.CLIENT_ADMIN));
                    criteriaList.add(Criteria.where("clientAdminId").is(requestDto.getClientAdminId()));
                }
            }
            if (requestDto.getAspireAdminId() != null && !requestDto.getAspireAdminId().isBlank()) {
                criteriaList.add(Criteria.where("aspireAdminId").is(requestDto.getAspireAdminId()));
            }
        }

        // Filter by end user (USER) for elevated roles and client admin
        if (resolvedUserType == UserType.MSP
                || resolvedUserType == UserType.ASPIRE_ADMIN
                || resolvedUserType == UserType.SUPER_ADMIN
                || resolvedUserType == UserType.SYSTEM_USER
                || resolvedUserType == UserType.CLIENT_ADMIN
                || resolvedUserType == UserType.CLIENT) {
            if (requestDto.getUser() != null && !requestDto.getUser().isBlank()) {
                if ("ALL".equalsIgnoreCase(requestDto.getUser())) {
                    criteriaList.add(Criteria.where("userType").is(UserType.USER));
                } else {
                    criteriaList.add(Criteria.where("userType").is(UserType.USER));
                    criteriaList.add(Criteria.where("userId").is(requestDto.getUser()));
                }
            }
        }

        // Filter by specific actor userId + userType from request
        applyUserIdAndUserTypeFilter(criteriaList, resolvedUserType, requestDto);

        if (requestDto.getIpAddress() != null && !requestDto.getIpAddress().isBlank()) {
            criteriaList.add(Criteria.where("ipAddress").is(requestDto.getIpAddress()));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        Query countQuery = new Query(criteria);

        List<ActivityLog> logs = mongoTemplate.find(query, ActivityLog.class);
        long total = mongoTemplate.count(countQuery, ActivityLog.class);

        Page<ActivityLog> activityLogPage = new PageImpl<>(logs, pageable, total);
        List<ActivityLogDto> activityLogDtos = activityLogPage.getContent().stream()
                .map(this::mapToActivityLogDto)
                .toList();

        return ActivityLogResponseDto.builder()
                .activityLogs(activityLogDtos)
                .totalElements(activityLogPage.getTotalElements())
                .totalPages(activityLogPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    /**
     * When request userId is provided, filter by that userId and request userType for:
     * ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER, MSP, CLIENT_ADMIN, and USER target types.
     * Allowed for callers: Aspire Admin, Super Admin, System User, MSP, Client Admin, and User.
     */
    private void applyUserIdAndUserTypeFilter(
            List<Criteria> criteriaList,
            UserType resolvedUserType,
            ActivityLogRequestDto requestDto) {
        if (requestDto.getUserId() == null || requestDto.getUserId().isBlank()) {
            return;
        }
        if (requestDto.getUserType() == null) {
            return;
        }

        boolean callerAllowed = resolvedUserType == UserType.ASPIRE_ADMIN
                || resolvedUserType == UserType.SUPER_ADMIN
                || resolvedUserType == UserType.SYSTEM_USER
                || resolvedUserType == UserType.MSP
                || resolvedUserType == UserType.CLIENT_ADMIN
                || resolvedUserType == UserType.CLIENT;
        if (!callerAllowed) {
            return;
        }

        UserType filterUserType = requestDto.getUserType();
        boolean targetAllowed = filterUserType == UserType.ASPIRE_ADMIN
                || filterUserType == UserType.SUPER_ADMIN
                || filterUserType == UserType.SYSTEM_USER
                || filterUserType == UserType.MSP
                || filterUserType == UserType.CLIENT_ADMIN;
        if (!targetAllowed) {
            return;
        }

        criteriaList.add(Criteria.where("userId").is(requestDto.getUserId()));
        criteriaList.add(Criteria.where("userType").is(filterUserType));
    }

    /** Escape special regex characters in search string to avoid invalid patterns. */
    private static String escapeRegex(String s) {
        if (s == null) return "";
        return s.replaceAll("([\\\\*+?|\\[\\](){}^$.])", "\\\\$1");
    }

    /**
     * Map ActivityLog entity to ActivityLogDto
     */
    private ActivityLogDto mapToActivityLogDto(ActivityLog activityLog) {
        String userName = activityLog.getUsername() != null ? activityLog.getUsername() : "";
        String userEmail = activityLog.getEmail() != null ? activityLog.getEmail() : "";
        String fullName = activityLog.getFullName() != null ? activityLog.getFullName() : "";

        return ActivityLogDto.builder()
                .activityId(activityLog.getId())
                .userId(activityLog.getUserId())
                .countryId(activityLog.getCountryId())
                .aspireAdminId(activityLog.getAspireAdminId())
                .mspId(activityLog.getMspId())
                .clientAdminId(activityLog.getClientAdminId())
                .userType(activityLog.getUserType())
                .userName(userName)
                .userEmail(userEmail)
                .fullName(fullName)
                .activityType(activityLog.getActivityType())
                .activityDescription(getActionDescription(activityLog.getActivityType()))
                .activityStatus(activityLog.getActivityStatus())
                .timestamp(activityLog.getCreatedAt())
                .ipAddress(activityLog.getIpAddress())
                .description(activityLog.getDescription())
                .oldValue(activityLog.getOldValue())
                .newValue(activityLog.getNewValue())
                .build();
    }
}
