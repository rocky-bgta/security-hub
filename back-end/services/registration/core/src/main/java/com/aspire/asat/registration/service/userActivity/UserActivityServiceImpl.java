package com.aspire.asat.registration.service.userActivity;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.TokenActionType;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.userActivity.UserLoginHistoryResponseDTO;
import com.aspire.asat.registration.data.userActivity.UserLoginStatisticsResponseDTO;
import com.aspire.asat.registration.enums.LoginHistoryFilterType;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.model.UserLoginHistory;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for User Activity operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {
    private static final String LOGIN_TIME = "loginTime";
    private static final String ACTION = "action";
    private static final String DATE_STRING = "dateString";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String UNKNOWN_DEVICE = "Unknown Device";
    public static final String LOGIN_COUNT = "loginCount";
    private final MongoTemplate mongoTemplate;
    private final UserCurrentContextService userCurrentContextService;
    private final ObjectMapper objectMapper;

    @Override
    public List<UserLoginStatisticsResponseDTO> getUserLoginStatistics(LoginHistoryFilterType filterType) {
        log.info("Getting user login statistics for filter type: {}", filterType);

        try {
            // Current user context for role-based filtering
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            String currentUsername = currentUserContext != null ? currentUserContext.getUsername() : null;
            String currentUserType = currentUserContext != null ? currentUserContext.getUserType() : null;
            String currentUserId = currentUserContext != null ? currentUserContext.getUserId() : null;

            log.info("Current user context - Username: {}, UserType: {}", currentUsername, currentUserType);

            // Calculate date range based on a filter type
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = calculateStartDate(filterType, endDate);

            // Convert LocalDate to Instant for MongoDB query
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            log.info("Querying login history from {} to {}", startDate, endDate);

            // Execute aggregation based on a filter type with role-based filtering
            List<UserLoginStatisticsResponseDTO> statistics = switch (filterType) {
                case SEVEN_DAYS -> getDailyStatistics(startInstant, endInstant, currentUserType, currentUserId);
                case ONE_MONTH -> getWeeklyStatistics(startInstant, endInstant, currentUserType, currentUserId);
                case TWELVE_MONTHS -> getMonthlyStatistics(startInstant, endInstant, currentUserType, currentUserId);
            };

            // Fill in missing periods with zero counts
            List<UserLoginStatisticsResponseDTO> completeStatistics = fillMissingPeriods(filterType, startDate, endDate, statistics);

            log.info("Successfully retrieved login statistics for {} periods using filter {}",
                    completeStatistics.size(), filterType);
            return completeStatistics;

        } catch (Exception e) {
            log.error("Error getting user login statistics: {}", e.getMessage(), e);
            throw new CustomException("Failed to get user login statistics", e);
        }
    }

    /**
     * Calculate the start date based on a filter type
     */
    private LocalDate calculateStartDate(LoginHistoryFilterType filterType, LocalDate endDate) {
        return switch (filterType) {
            case SEVEN_DAYS -> endDate.minusDays(6); // Last 7 days (including today)
            case ONE_MONTH -> endDate.minusDays(29); // Last 30 days (4 weeks)
            case TWELVE_MONTHS -> endDate.minusMonths(11).withDayOfMonth(1); // Last 12 months
        };
    }

    /**
     * Get daily statistics for 7Day filter with role-based filtering
     */
    private List<UserLoginStatisticsResponseDTO> getDailyStatistics(Instant startInstant, Instant endInstant,
                                                                    String currentUserType, String currentUserId) {
        // Include both LOGIN and LOGOUT actions, filter by time range
        Criteria criteria = Criteria.where(LOGIN_TIME).gte(startInstant).lte(endInstant)
                .orOperator(
                        Criteria.where(ACTION).is(TokenActionType.LOGIN)
//                        Criteria.where(ACTION).is(TokenActionType.LOGOUT)
                );

        // Apply role-based filtering
        applyRoleBasedFiltering(criteria, currentUserType, currentUserId);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project()
                        .andExpression("dateToString('%Y-%m-%d', loginTime)").as(DATE_STRING)
                        .andExpression("year(loginTime)").as("year")
                        .andExpression("month(loginTime)").as("month")
                        .andExpression("dayOfMonth(loginTime)").as("day"),
                Aggregation.group(DATE_STRING, "year", "month", "day")
                        .count().as(LOGIN_COUNT),
                Aggregation.sort(Sort.by("year", "month", "day").ascending())
        );

        return executeAggregation(aggregation, "daily");
    }

    /**
     * Get weekly statistics for 1. Month filter with role-based filtering
     */
    private List<UserLoginStatisticsResponseDTO> getWeeklyStatistics(Instant startInstant, Instant endInstant,
                                                                     String currentUserType, String currentUserId) {
        // Include both LOGIN and LOGOUT actions, filter by time range
        Criteria criteria = Criteria.where(LOGIN_TIME).gte(startInstant).lte(endInstant)
                .orOperator(
                        Criteria.where(ACTION).is(TokenActionType.LOGIN)
//                        Criteria.where(ACTION).is(TokenActionType.LOGOUT)
                );

        // Apply role-based filtering
        applyRoleBasedFiltering(criteria, currentUserType, currentUserId);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project()
                        .andExpression("isoWeekYear(loginTime)").as("isoYear")
                        .andExpression("isoWeek(loginTime)").as("week")
                        .andExpression("year(loginTime)").as("calendarYear"),
                Aggregation.group("isoYear", "week", "calendarYear")
                        .count().as(LOGIN_COUNT),
                Aggregation.sort(Sort.by("isoYear", "week").ascending())
        );

        return executeAggregation(aggregation, "weekly");
    }

    /**
     * Get monthly statistics for 12. Month filter with role-based filtering
     */
    private List<UserLoginStatisticsResponseDTO> getMonthlyStatistics(Instant startInstant, Instant endInstant,
                                                                      String currentUserType, String currentUserId) {
        // Include both LOGIN and LOGOUT actions, filter by time range
        Criteria criteria = Criteria.where(LOGIN_TIME).gte(startInstant).lte(endInstant)
                .orOperator(
                        Criteria.where(ACTION).is(TokenActionType.LOGIN)
//                        Criteria.where(ACTION).is(TokenActionType.LOGOUT)
                );

        // Apply role-based filtering
        applyRoleBasedFiltering(criteria, currentUserType, currentUserId);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project()
                        .andExpression("dateToString('%Y-%m', loginTime)").as("monthString")
                        .andExpression("year(loginTime)").as("year")
                        .andExpression("month(loginTime)").as("month"),
                Aggregation.group("monthString", "year", "month")
                        .count().as(LOGIN_COUNT),
                Aggregation.sort(Sort.by("year", "month").ascending())
        );

        return executeAggregation(aggregation, "monthly");
    }

    /**
     * Apply role-based filtering to criteria
     * Admin users (SUPER_ADMIN, ASPIRE_ADMIN) see all data
     * Client Admin users see only data for their client
     * Regular users see only their own data
     */
    private void applyRoleBasedFiltering(Criteria criteria, String currentUserType, String currentUserId) {
        /*if (isAdminUser(currentUserType)) {
            log.debug("Admin user - no username filter applied");
        } else if (isClientAdmin(currentUserType)) {
            log.debug("Client admin - filtering by Used Id of client Admin: {}", currentUserId);
            criteria.and("userId").is(currentUserId);
        } else {
            log.debug("Regular user - filtering by username: {}", currentUserId);
            criteria.and("userId").is(currentUserId);
        }*/

        // Every user will see their own login statistics
        criteria.and("userId").is(currentUserId);
    }

    /**
     * Execute aggregation and convert results to DTOs
     */
    private List<UserLoginStatisticsResponseDTO> executeAggregation(Aggregation aggregation, String periodType) {
        long startTime = System.currentTimeMillis();
        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation,
                UserLoginHistory.class,
                Map.class
        );
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("MongoDB aggregation executed in {} ms for {}", executionTime, periodType);

        List<Map> aggregationResults = results.getMappedResults();

        return aggregationResults.stream()
                .map(result -> convertToDTO(result, periodType))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Convert aggregation result to DTO
     */
    private UserLoginStatisticsResponseDTO convertToDTO(Map<String, Object> result, String periodType) {
        try {
            Object idObj = result.get("_id");
            Long loginCount = getLongValue(result, LOGIN_COUNT);

            if (idObj == null) {
                log.warn("Skipping invalid aggregation result: {}", result);
                return null;
            }

            // Default to 0 if count is null
            loginCount = loginCount != null ? loginCount : 0L;

            Map<String, Object> idMap = (Map<String, Object>) idObj;

            return switch (periodType) {
                case "daily" -> createDailyDTO(idMap, loginCount);
                case "weekly" -> createWeeklyDTO(idMap, loginCount);
                case "monthly" -> createMonthlyDTO(idMap, loginCount);
                default -> null;
            };
        } catch (Exception e) {
            log.error("Error processing aggregation result: {}", result, e);
            return null;
        }
    }

    /**
     * Create daily DTO
     */
    private UserLoginStatisticsResponseDTO createDailyDTO(Map<String, Object> idMap, Long loginCount) {
        String dateString = getStringValue(idMap, DATE_STRING);
        Integer year = getIntValue(idMap, "year");
        Integer month = getIntValue(idMap, "month");
        Integer day = getIntValue(idMap, "day");

        if (dateString == null || year == null || month == null || day == null) {
            return null;
        }

        LocalDate date = LocalDate.of(year, month, day);
        String dayName = date.getDayOfWeek().toString(); // Saturday, Sunday, Monday, etc.

        return UserLoginStatisticsResponseDTO.builder()
                .date(date)
                .loginCount(loginCount)
                .dateString(dateString)
                .monthName(dayName)
                .year(year)
                .month(month)
                .build();
    }

    /**
     * Create weekly DTO
     */
    private UserLoginStatisticsResponseDTO createWeeklyDTO(Map<String, Object> idMap, Long loginCount) {
        Integer isoYear = getIntValue(idMap, "isoYear"); // ISO week-based year
        Integer week = getIntValue(idMap, "week"); // ISO week number
        Integer calendarYear = getIntValue(idMap, "calendarYear"); // Calendar year for display

        if (isoYear == null || week == null) {
            return null;
        }

        // Calculate the date from ISO week-based year and week (first day of the week - Monday)
        // Use January 4th as base date (always in week 1 of the ISO week-based year)
        WeekFields weekFields = WeekFields.ISO;
        LocalDate baseDate = LocalDate.of(isoYear, 1, 4);
        LocalDate date = baseDate
                .with(weekFields.weekOfWeekBasedYear(), week)
                .with(weekFields.dayOfWeek(), 1); // Monday (ISO week starts on Monday)

        String dateString = date.format(DateTimeFormatter.ofPattern(YYYY_MM_DD));

        // Create week name like "1st Week", "2nd Week", etc.
        String weekName = getWeekName(week);

        // Use calendar year for display purposes
        int displayYear = calendarYear != null ? calendarYear : date.getYear();

        return UserLoginStatisticsResponseDTO.builder()
                .date(date)
                .loginCount(loginCount)
                .dateString(dateString)
                .monthName(weekName)
                .year(displayYear)
                .month(date.getMonthValue())
                .build();
    }

    /**
     * Create monthly DTO
     */
    private UserLoginStatisticsResponseDTO createMonthlyDTO(Map<String, Object> idMap, Long loginCount) {
        String monthString = getStringValue(idMap, "monthString");
        Integer year = getIntValue(idMap, "year");
        Integer month = getIntValue(idMap, "month");

        if (monthString == null || year == null || month == null) {
            return null;
        }

        LocalDate date = LocalDate.of(year, month, 1);

        return UserLoginStatisticsResponseDTO.builder()
                .date(date)
                .loginCount(loginCount)
                .dateString(monthString)
                .monthName(date.getMonth().toString())
                .year(year)
                .month(month)
                .build();
    }

    /**
     * Fill in missing periods with zero counts
     */
    private List<UserLoginStatisticsResponseDTO> fillMissingPeriods(
            LoginHistoryFilterType filterType,
            LocalDate startDate,
            LocalDate endDate,
            List<UserLoginStatisticsResponseDTO> existingStatistics) {

        Map<String, UserLoginStatisticsResponseDTO> statisticsMap = existingStatistics.stream()
                .collect(Collectors.toMap(UserLoginStatisticsResponseDTO::getDateString, dto -> dto));


        return switch (filterType) {
            case SEVEN_DAYS -> fillMissingDays(startDate, endDate, statisticsMap);
            case ONE_MONTH -> fillMissingWeeks(startDate, endDate, statisticsMap);
            case TWELVE_MONTHS -> fillMissingMonths(startDate, endDate, statisticsMap);
        };
    }

    /**
     * Fill missing days
     */
    private List<UserLoginStatisticsResponseDTO> fillMissingDays(
            LocalDate startDate,
            LocalDate endDate,
            Map<String, UserLoginStatisticsResponseDTO> statisticsMap) {

        List<UserLoginStatisticsResponseDTO> completeStatistics = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            String dateString = currentDate.format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
            UserLoginStatisticsResponseDTO dto = statisticsMap.get(dateString);

            if (dto == null) {
                String dayName = currentDate.getDayOfWeek().toString(); // Saturday, Sunday, Monday, etc.
                dto = UserLoginStatisticsResponseDTO.builder()
                        .date(currentDate)
                        .loginCount(0L)
                        .dateString(dateString)
                        .monthName(dayName)
                        .year(currentDate.getYear())
                        .month(currentDate.getMonthValue())
                        .build();
            }
            completeStatistics.add(dto);
            currentDate = currentDate.plusDays(1);
        }

        return completeStatistics;
    }

    /**
     * Fill missing weeks
     * For ONE_MONTH filter, always returns exactly 5 weeks.
     * The 5th week will contain the remaining days after the first 4 weeks (days 29-30).
     */
    private List<UserLoginStatisticsResponseDTO> fillMissingWeeks(
            LocalDate startDate,
            LocalDate endDate,
            Map<String, UserLoginStatisticsResponseDTO> statisticsMap) {

        List<UserLoginStatisticsResponseDTO> completeStatistics = new ArrayList<>();
        WeekFields weekFields = WeekFields.ISO;
        
        // Ensure we start from Monday of the week containing startDate
        LocalDate currentDate = startDate.with(weekFields.dayOfWeek(), 1); // Monday (ISO week starts on Monday)
        
        // For ONE_MONTH filter, always return exactly 5 weeks
        // The 5th week will naturally contain the remaining days (days 29-30) after 4 weeks
        for (int weekIndex = 0; weekIndex < 5; weekIndex++) {
            String dateString = currentDate.format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
            UserLoginStatisticsResponseDTO dto = statisticsMap.get(dateString);

            if (dto == null) {
                int weekOfYear = currentDate.get(weekFields.weekOfWeekBasedYear());
                String weekName = getWeekName(weekOfYear);
                dto = UserLoginStatisticsResponseDTO.builder()
                        .date(currentDate)
                        .loginCount(0L)
                        .dateString(dateString)
                        .monthName(weekName)
                        .year(currentDate.getYear())
                        .month(currentDate.getMonthValue())
                        .build();
            }
            completeStatistics.add(dto);
            currentDate = currentDate.plusWeeks(1);
        }

        return completeStatistics;
    }

    /**
     * Fill missing months
     */
    private List<UserLoginStatisticsResponseDTO> fillMissingMonths(
            LocalDate startDate,
            LocalDate endDate,
            Map<String, UserLoginStatisticsResponseDTO> statisticsMap) {

        List<UserLoginStatisticsResponseDTO> completeStatistics = new ArrayList<>();
        LocalDate currentDate = startDate.withDayOfMonth(1);
        LocalDate endMonth = endDate.withDayOfMonth(1);

        while (!currentDate.isAfter(endMonth)) {
            String monthString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            UserLoginStatisticsResponseDTO dto = statisticsMap.get(monthString);

            if (dto == null) {
                dto = UserLoginStatisticsResponseDTO.builder()
                        .date(currentDate)
                        .loginCount(0L)
                        .dateString(monthString)
                        .monthName(currentDate.getMonth().toString())
                        .year(currentDate.getYear())
                        .month(currentDate.getMonthValue())
                        .build();
            }
            completeStatistics.add(dto);
            currentDate = currentDate.plusMonths(1);
        }

        return completeStatistics;
    }

    /**
     * Safe method to get string value from map with null handling
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Safe method to get long value from map with null handling
     */
    private Long getLongValue(Map<String, Object> map) {
        return getLongValue(map, LOGIN_COUNT);
    }

    /**
     * Safe method to get long value from map with null handling for a specific key
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert value to Long: {} for key: {}", value, key);
            return null;
        }
    }

    /**
     * Safe method to get integer value from map with null handling
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert value to Integer: {} for key: {}", value, key);
            return null;
        }
    }

    /**
     * Get week name in ordinal format (1st Week, 2nd Week, etc.)
     */
    private String getWeekName(int weekNumber) {
        return switch (weekNumber) {
            case 1 -> "1st Week";
            case 2 -> "2nd Week";
            case 3 -> "3rd Week";
            case 4 -> "4th Week";
            case 5 -> "5th Week";
            case 6 -> "6th Week";
            case 7 -> "7th Week";
            case 8 -> "8th Week";
            case 9 -> "9th Week";
            case 10 -> "10th Week";
            case 11 -> "11th Week";
            case 12 -> "12th Week";
            case 13 -> "13th Week";
            case 14 -> "14th Week";
            case 15 -> "15th Week";
            case 16 -> "16th Week";
            case 17 -> "17th Week";
            case 18 -> "18th Week";
            case 19 -> "19th Week";
            case 20 -> "20th Week";
            case 21 -> "21st Week";
            case 22 -> "22nd Week";
            case 23 -> "23rd Week";
            case 24 -> "24th Week";
            case 25 -> "25th Week";
            case 26 -> "26th Week";
            case 27 -> "27th Week";
            case 28 -> "28th Week";
            case 29 -> "29th Week";
            case 30 -> "30th Week";
            case 31 -> "31st Week";
            case 32 -> "32nd Week";
            case 33 -> "33rd Week";
            case 34 -> "34th Week";
            case 35 -> "35th Week";
            case 36 -> "36th Week";
            case 37 -> "37th Week";
            case 38 -> "38th Week";
            case 39 -> "39th Week";
            case 40 -> "40th Week";
            case 41 -> "41st Week";
            case 42 -> "42nd Week";
            case 43 -> "43rd Week";
            case 44 -> "44th Week";
            case 45 -> "45th Week";
            case 46 -> "46th Week";
            case 47 -> "47th Week";
            case 48 -> "48th Week";
            case 49 -> "49th Week";
            case 50 -> "50th Week";
            case 51 -> "51st Week";
            case 52 -> "52nd Week";
            case 53 -> "53rd Week";
            default -> weekNumber + "th Week";
        };
    }

    @Override
    public AllResponseDto<List<UserLoginHistoryResponseDTO>> getUserLoginHistory(String actionType, LocalDate startDate,
                                                                                 LocalDate endDate, String username, int offset, int pageSize) {
        log.info("Getting user login history with actionType: {}, startDate: {}, endDate: {}, username: {}, offset: {}, pageSize: {}",
                actionType, startDate, endDate, username, offset, pageSize);

        try {
            // Current user context
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            String currentUsername = currentUserContext != null ? currentUserContext.getUsername() : null;
            String currentUserType = currentUserContext != null ? currentUserContext.getUserType() : null;
            String currentUserId = currentUserContext != null ? currentUserContext.getUserId() : null;

            log.info("Current user context - Username: {}, UserType: {}", currentUsername, currentUserType);

            // Validate pagination
            offset = Math.max(0, offset);
            if (pageSize <= 0) {
                log.warn("Invalid pageSize: {}. Defaulting to 10.", pageSize);
                pageSize = 10;
            }

            ZoneId zone = ZoneId.systemDefault();
            Query query = new Query();

            // Role-based filtering
            if (isAdminUser(currentUserType)) {
                // Aspire Admin: Can see all users' login history
                log.info("Admin user - showing all users' login history");
            } else if (isClientAdmin(currentUserType)) {
                // Client Admin: Can see their own and their users' login history
                query.addCriteria(Criteria.where("clientAdminId").is(currentUserId));
                log.info("Client Admin user - showing login history for clientAdminId: {}", currentUserId);
            } else {
                // Regular User: Can only see their own login history
                query.addCriteria(Criteria.where("userId").is(currentUserId));
                log.info("Regular user - showing only own login history for userId: {}", currentUserId);
            }

            // Action type filter
            if (actionType != null && !actionType.trim().isEmpty()) {
                try {
                    TokenActionType tokenAction = TokenActionType.valueOf(actionType.trim().toUpperCase());
                    query.addCriteria(Criteria.where(ACTION).is(tokenAction));
                    log.debug("Action type filter: {}", tokenAction);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid action type: {}. Valid values are LOGIN, LOGOUT", actionType);
                    throw new CustomException("Invalid action type. Valid values are LOGIN, LOGOUT");
                }
            }

            // Username search filter
            if (username != null && !username.trim().isEmpty()) {
                query.addCriteria(Criteria.where("username").regex(username.trim(), "i")); // Case-insensitive search
                log.debug("Username search filter: {}", username);
            }

            // Date range filters - combine into single criteria to avoid duplicate field error
            if (startDate != null || endDate != null) {
                Criteria dateCriteria = Criteria.where(LOGIN_TIME);
                if (startDate != null) {
                    Instant startInstant = startDate.atStartOfDay(zone).toInstant();
                    dateCriteria.gte(startInstant);
                    log.debug("Start date filter: {} -> {}", startDate, startInstant);
                }
                if (endDate != null) {
                    Instant endInstant = endDate.atTime(23, 59, 59).atZone(zone).toInstant();
                    dateCriteria.lte(endInstant);
                    log.debug("End date filter: {} -> {}", endDate, endInstant);
                }
                query.addCriteria(dateCriteria);
            }

            // Sorting and count
            query.with(Sort.by(Sort.Direction.DESC, LOGIN_TIME));
            long totalCount = mongoTemplate.count(query, UserLoginHistory.class);
            log.info("Total records found: {}", totalCount);

            // Pagination and fetch
            Pageable pageable = PageRequest.of(offset, pageSize);
            query.with(pageable);
            List<UserLoginHistory> userLoginHistories = mongoTemplate.find(query, UserLoginHistory.class);
            log.info("Retrieved {} records for page offset: {}", userLoginHistories.size(), offset);

            // Convert to DTOs
            List<UserLoginHistoryResponseDTO> responseData = userLoginHistories.stream()
                    .map(this::convertToUserLoginHistoryResponseDTO)
                    .sorted(Comparator.comparing(UserLoginHistoryResponseDTO::getTimestamp,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            log.info("Successfully retrieved user login history: {} records, page {} of {}",
                    responseData.size(), offset + 1, totalPages);

            return new AllResponseDto<>(offset, pageSize, totalCount, responseData);

        } catch (Exception e) {
            log.error("Error getting user login history", e);
            throw new CustomException("Failed to get user login history", e);
        }
    }

    /**
     * Convert UserLoginHistory entity to response DTO
     */
    private UserLoginHistoryResponseDTO convertToUserLoginHistoryResponseDTO(UserLoginHistory userLoginHistory) {
        Instant timestamp;
        if (userLoginHistory.getAction() == TokenActionType.LOGOUT) {
            timestamp = userLoginHistory.getLogoutTime() != null
                    ? userLoginHistory.getLogoutTime()
                    : userLoginHistory.getLoginTime();
        } else {
            timestamp = userLoginHistory.getLoginTime();
        }

        return UserLoginHistoryResponseDTO.builder()
                .username(userLoginHistory.getUsername())
                .actionType(userLoginHistory.getAction() != null ? userLoginHistory.getAction().name() : null)
                .timestamp(timestamp)
                .deviceInfo(formatDeviceInfo(userLoginHistory.getDeviceInfo()))
                .userType(userLoginHistory.getUserType())
                .requestIp(null) // currently hide request IP
                .build();
    }

    /**
     * Format device info JSON string into human-readable format
     */
    private String formatDeviceInfo(String deviceInfoJson) {
        if (deviceInfoJson == null || deviceInfoJson.trim().isEmpty()) {
            return UNKNOWN_DEVICE;
        }

        try {
            JsonNode deviceNode = objectMapper.readTree(deviceInfoJson);

            String platformType = getJsonValue(deviceNode, "platformType");
            String platformInfo = getJsonValue(deviceNode, "platformInfo");
            String platformVersion = getJsonValue(deviceNode, "platformVersion");
            String appLanguage = getJsonValue(deviceNode, "appLanguage");
            String appVersion = getJsonValue(deviceNode, "appVersion");

            StringBuilder formattedInfo = new StringBuilder();

            appendIfNotNull(formattedInfo, platformType, "");
            appendIfNotNull(formattedInfo, platformInfo, formattedInfo.isEmpty() ? "" : " - ");
            appendIfNotNull(formattedInfo, platformVersion, " ");

            if (appLanguage != null) {
                formattedInfo.append(formattedInfo.isEmpty() ? "" : " (").append(appLanguage);
            }

            if (appVersion != null) {
                if (appLanguage != null) {
                    formattedInfo.append(", v").append(appVersion).append(")");
                } else {
                    formattedInfo.append(" v").append(appVersion);
                }
            }

            return !formattedInfo.isEmpty() ? formattedInfo.toString() : UNKNOWN_DEVICE;

        } catch (Exception e) {
            log.warn("Failed to parse device info JSON: {}", deviceInfoJson, e);
            return UNKNOWN_DEVICE;
        }
    }

    private void appendIfNotNull(StringBuilder sb, String value, String prefix) {
        if (value != null) {
            sb.append(prefix).append(value);
        }
    }

    /**
     * Safely extract string value from the JSON node
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * Check if the user is an admin user
     * Admin users include SUPER_ADMIN, ASPIRE_ADMIN, and SYSTEM_USER (same access as ASPIRE_ADMIN)
     */
    private boolean isAdminUser(String userType) {
        if (userType == null) {
            return false;
        }
        return userType.equalsIgnoreCase(UserType.SUPER_ADMIN.getValue())
                || userType.equalsIgnoreCase(UserType.ASPIRE_ADMIN.getValue())
                || userType.equalsIgnoreCase(UserType.SYSTEM_USER.getValue());
    }

    private boolean isClientAdmin(String userType) {
        if (userType == null) {
            return false;
        }
        return userType.equalsIgnoreCase(UserType.CLIENT_ADMIN.getValue());
    }
}