package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.cms.ClientAdminInfoDto;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.registration.SubPackageAssignRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Client for communicating with the Registration Service.
 * Used to fetch user, department, and group data for campaign recipients.
 *
 * Calls GET {@code /end-user/users} on the registration service, which supports filtering
 * by clientAdminId, departments, and riskGroup with pagination (ACTIVE users only server-side).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceClient {

    private static final int FETCH_PAGE_SIZE = 1000;
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.registration.url}")
    private String registrationUrl;

    /**
     * Get all active users for a client.
     */
    public List<UserDto> getAllUsers(String clientId) {
        log.info("Fetching all active users for client: {}", clientId);
        return fetchAllUsersPaginated(clientId, null, null);
    }

    /**
     * Get users by specific user IDs.
     * Fetches all active users and filters in-memory since the registration API
     * does not provide a batch-by-IDs endpoint.
     */
    public List<UserDto> getUsersByIds(String clientId, List<String> userIds) {
        log.info("Fetching {} users by IDs for client: {}", userIds.size(), clientId);
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> idSet = Set.copyOf(userIds);
        List<UserDto> allUsers = fetchAllUsersPaginated(clientId, null, null);

        return allUsers.stream()
                .filter(user -> idSet.contains(user.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Get users filtered by department names.
     */
    public List<UserDto> getUsersByDepartments(String clientId, List<String> departmentIds) {
        log.info("Fetching users from {} departments for client: {}", departmentIds.size(), clientId);
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchAllUsersPaginated(clientId, departmentIds, null);
    }

    /**
     * Get users by group IDs.
     * The registration service end-user API does not support group-based filtering.
     * Falls back to fetching all users with a warning.
     */
    public List<UserDto> getUsersByGroups(String clientId, List<String> groupIds) {
        log.warn("Group-based filtering is not supported by the registration end-user API. " +
                "Falling back to fetching all active users for client: {}", clientId);
        return fetchAllUsersPaginated(clientId, null, groupIds);
    }

    /**
     * Get all departments for a client.
     * Not yet supported -- the registration service does not expose a departments list API.
     */
    public List<DepartmentDto> getDepartments(String clientId) {
        log.warn("getDepartments is not yet implemented. Registration service does not expose a departments list API.");
        return Collections.emptyList();
    }

    /**
     * Get all groups for a client.
     * Not yet supported -- the registration service does not expose a groups list API.
     */
    public List<GroupDto> getGroups(String clientId) {
        log.warn("getGroups is not yet implemented. Registration service does not expose a groups list API.");
        return Collections.emptyList();
    }

    /**
     * Get total active user count for a client.
     */
    public int getUserCount(String clientId) {
        log.info("Fetching user count for client: {}", clientId);
        return fetchTotalCount(clientId, null, null);
    }

    /**
     * Get user count filtered by departments.
     */
    public int getUserCountByDepartments(String clientId, List<String> departmentIds) {
        log.info("Fetching user count for {} departments, client: {}", departmentIds.size(), clientId);
        return fetchTotalCount(clientId, departmentIds, null);
    }

    /**
     * Get user count by groups.
     * Group filtering is not supported; falls back to total count.
     */
    public int getUserCountByGroups(String clientId, List<String> groupIds) {
        log.warn("Group-based count is not supported. Returning total active user count for client: {}", clientId);
        return fetchTotalCount(clientId, null, null);
    }

    /**
     * Call registration service PUT /end-user/risk-profile-exist to bulk update
     * isRiskProfileExist for the given user IDs.
     *
     * @param userIds           list of user IDs to update
     * @param isRiskProfileExist value to set (typically true)
     */
    public void updateRiskProfileExist(List<String> userIds, boolean isRiskProfileExist) {
        if (userIds == null || userIds.isEmpty()) {
            log.debug("updateRiskProfileExist called with empty userIds, skipping");
            return;
        }
        String url = registrationUrl + "/end-user/risk-profile-exist";
        Map<String, Object> body = Map.of(
                "userIds", userIds,
                "isRiskProfileExist", isRiskProfileExist
        );
        log.info("Calling registration PUT risk-profile-exist for {} users", userIds.size());
        try {
            webClient.put()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.debug("Risk profile exist updated successfully for {} users", userIds.size());
        } catch (Exception e) {
            log.error("Error calling registration updateRiskProfileExist for {} userIds: {}", userIds.size(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Call registration PUT /end-user/{userId}/risk-group to update AspireUser.riskGroup.
     *
     * @param userId   Aspire userId (UUID as string)
     * @param riskLevel phishing risk level (LOW/MEDIUM/HIGH/CRITICAL)
     */
    public void updateUserRiskGroup(String userId, RiskLevel riskLevel) {
        if (userId == null || userId.isBlank() || riskLevel == null) {
            log.debug("updateUserRiskGroup called with invalid args, skipping (userId={}, riskLevel={})", userId, riskLevel);
            return;
        }

        String riskGroup = switch (riskLevel) {
            case LOW -> "LOW_RISK";
            case MEDIUM -> "MEDIUM_RISK";
            case HIGH -> "HIGH_RISK";
            case CRITICAL -> "CRITICAL_RISK";
        };

        String url = registrationUrl + "/end-user/" + userId.trim() + "/risk-group";
        Map<String, Object> body = Map.of("riskGroup", riskGroup);

        log.info("Calling registration PUT risk-group for userId={} riskGroup={}", userId, riskGroup);
        try {
            webClient.put()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Error calling registration updateUserRiskGroup userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Assign a sub-package to a single user via registration service.
     */
    public void assignSubPackageToUser(SubPackageAssignRequestDto request) {
        String url = registrationUrl + "/end-user/assign-sub-package";
        log.info("Calling registration POST assign-sub-package");
        try {
            webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Sub-package assigned successfully");
        } catch (Exception e) {
            log.error("Error calling registration assignSubPackageToUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Resolves client admin IDs belonging to an MSP via
     * {@code GET /client/admin/get-client-by-msp?mspId=...}.
     */
    public List<String> getClientAdminIdsByMspId(String mspId) {
        if (mspId == null || mspId.isBlank()) {
            return Collections.emptyList();
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(registrationUrl + "/client/admin/get-client-by-msp")
                .queryParam("mspId", mspId.trim())
                .toUriString();
        log.info("Fetching client admin IDs for mspId={}", mspId);
        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null || !response.get("data").isArray()) {
                log.warn("No client admin data returned for mspId={}", mspId);
                return Collections.emptyList();
            }

            List<String> ids = new ArrayList<>();
            for (JsonNode node : response.get("data")) {
                String id = getTextOrNull(node, "id");
                if (id != null && !id.isBlank()) {
                    ids.add(id);
                }
            }
            log.info("Resolved {} client admin IDs for mspId={}", ids.size(), mspId);
            return ids;
        } catch (Exception e) {
            log.error("Error fetching client admin IDs for mspId={}: {}", mspId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetch lightweight client admin info (country, complianceId, industry, subIndustryId)
     * for topic recommendation filtering.
     */
    public ClientAdminInfoDto getClientAdminInfo(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            log.warn("getClientAdminInfo called with blank clientAdminId");
            return new ClientAdminInfoDto();
        }
        String url = registrationUrl + "/client/admin/" + clientAdminId.trim();
        log.info("Fetching client admin info for topic recommendation: {}", clientAdminId);
        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data in registration client admin response for id: {}", clientAdminId);
                return new ClientAdminInfoDto();
            }

            JsonNode data = response.get("data");
            return ClientAdminInfoDto.builder()
                    .country(getTextOrNull(data, "country") != null
                            ? getNestedId(data, "country") : null)
                    .complianceId(getTextOrNull(data, "complianceId"))
                    .industry(getNestedId(data, "industry"))
                    .subIndustryId(getNestedId(data, "subIndustry"))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching client admin info for id: {}", clientAdminId, e);
            return new ClientAdminInfoDto();
        }
    }

    private String getNestedId(JsonNode parent, String field) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) return null;
        if (node.isObject()) {
            JsonNode idNode = node.get("id");
            return (idNode != null && !idNode.isNull()) ? idNode.asText() : null;
        }
        return node.asText();
    }

    /**
     * Resolves a registration timezone dropdown document id to display name and IANA zone id.
     */
    public Optional<RegistrationTimezone> getTimezoneByDocumentId(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return Optional.empty();
        }
        String url = registrationUrl + "/dropdown/timezones/" + documentId.trim();
        return fetchTimezone(url, "documentId", documentId.trim());
    }

    /**
     * Resolves a registration timezone by its {@code timezoneId} field (e.g. {@code UTC}, {@code Asia/Dhaka}).
     */
    public Optional<RegistrationTimezone> getTimezoneByTimezoneId(String timezoneId) {
        if (timezoneId == null || timezoneId.isBlank()) {
            return Optional.empty();
        }
        String ref = timezoneId.trim();
        String url = registrationUrl + "/dropdown/timezones/timezone-id/" + ref;
        return fetchTimezone(url, "timezoneId", ref);
    }

    private Optional<RegistrationTimezone> fetchTimezone(String url, String keyLabel, String keyValue) {
        log.info("Fetching timezone from registration: GET {}", url);
        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null || response.get("data").isNull()) {
                log.warn("No timezone data in registration response for {}: {}", keyLabel, keyValue);
                return Optional.empty();
            }

            JsonNode data = response.get("data");
            String displayName = getTextOrNull(data, "displayName");
            String ianaZoneId = getTextOrNull(data, "timezoneId");

            log.info("Fetched timezone for {} {}: displayName='{}', timezoneId='{}'",
                    keyLabel, keyValue, displayName, ianaZoneId);
            return Optional.of(new RegistrationTimezone(displayName, ianaZoneId != null ? ianaZoneId.trim() : null));
        } catch (WebClientResponseException e) {
            log.warn("Registration timezone lookup failed for {} {}: HTTP {} - {}",
                    keyLabel, keyValue, e.getStatusCode().value(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching timezone by {}: {}", keyLabel, keyValue, e);
            return Optional.empty();
        }
    }

    /**
     * @deprecated Prefer {@link #getTimezoneByDocumentId(String)} so IANA {@code timezoneId} is available for parsing.
     */
    @Deprecated
    public Optional<String> getTimezoneDisplayNameById(String timeZoneId) {
        return getTimezoneByDocumentId(timeZoneId).map(RegistrationTimezone::displayName);
    }

    // --- Private helpers ---

    /**
     * Fetches all users across pages by calling the registration end-user API repeatedly.
     */
    private List<UserDto> fetchAllUsersPaginated(String clientId, List<String> departments,  List<String> riskGroupids ) {
        List<UserDto> allUsers = new ArrayList<>();
        int offset = 0;
        long total;

        FetchResult result = fetchUsersPage(clientId, departments, riskGroupids, offset, FETCH_PAGE_SIZE);

        allUsers.addAll(result.users);


        log.info("Fetched {} total users for client: {}", allUsers.size(), clientId);
        return allUsers;
    }

    /**
     * Fetches the total count without loading all user data (uses pageSize=1).
     */
    private int fetchTotalCount(String clientId, List<String> departments, List<String> riskGroups) {
        FetchResult result = fetchUsersPage(clientId, departments,  riskGroups, 0, 1);
        if (result == null) {
            return 0;
        }
        return (int) result.total;
    }

    /**
     * Calls GET /end-user with the given parameters and parses the response.
     *
     * Registration API response structure:
     * { "message": "...", "statusCode": 200, "data": { "offset": 0, "pageSize": 10, "total": 150, "items": [...] } }
     */
    private FetchResult fetchUsersPage(String clientId, List<String> departments, List<String> riskGroupids, int offset, int pageSize) {
        String url = buildUrl(clientId, departments, riskGroupids, offset, pageSize);
        log.debug("Calling Registration API: {}", url);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data in registration response for client: {}", clientId);
                return null;
            }

            JsonNode dataNode = response.get("data");
            long total = dataNode.has("total") ? dataNode.get("total").asLong(0) : 0;

            JsonNode itemsNode = dataNode.get("items");
            if (itemsNode == null || !itemsNode.isArray()) {
                log.warn("No items array in registration response for client: {}", clientId);
                return new FetchResult(Collections.emptyList(), total);
            }

            List<JsonNode> rawUsers = objectMapper.readerFor(new TypeReference<List<JsonNode>>() {}).readValue(itemsNode);

            List<UserDto> users = rawUsers.stream()
                    .map(this::mapToUserDto)
                    .collect(Collectors.toList());

            log.debug("Fetched page: offset={}, pageSize={}, returned={}, total={}", offset, pageSize, users.size(), total);
            return new FetchResult(users, total);

        } catch (Exception e) {
            log.error("Error calling registration service for client: {}, offset: {}", clientId, offset, e);
            return null;
        }
    }

    private String buildUrl(String clientId, List<String> departments, List<String> riskGroupIds, int offset, int pageSize) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(registrationUrl + "/end-user/users")
                .queryParam("clientAdminId", clientId)
//                .queryParam("status", ACTIVE_STATUS)
                .queryParam("offset", offset)
                .queryParam("pageSize", pageSize);

        if (departments != null && !departments.isEmpty()) {
            builder.queryParam("departments", departments.toArray());
        }

        if (riskGroupIds != null && !riskGroupIds.isEmpty()) {
            builder.queryParam("riskGroup", riskGroupIds.toArray());
            // riskGroupIds.forEach(rg -> builder.queryParam("riskGroup", normalizeQueryParamForUri(rg)));
        }
        String finalUrl = builder.build(false).toUriString();

        return finalUrl;
    }

    /**
     * UriComponentsBuilder encodes query values once. Callers sometimes pass values that are already
     * percent-encoded once or twice (e.g. {@code Information%20...} or {@code Information%2520...}).
     * That produces {@code %2520} in the final URL and the registration service matches the wrong string.
     * Decode repeatedly until stable so we pass plain text and only the builder encodes.
     */
    private static String normalizeQueryParamForUri(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String current = value;
        for (int i = 0; i < 10; i++) {
            if (!current.contains("%")) {
                break;
            }
            try {
                String decoded = URLDecoder.decode(current, StandardCharsets.UTF_8);
                if (decoded.equals(current)) {
                    break;
                }
                current = decoded;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        return current;
    }

    private UserDto mapToUserDto(JsonNode node) {
        return UserDto.builder()
                .userId(getTextOrNull(node, "id"))
                .email(getTextOrNull(node, "email"))
                .firstName(getTextOrNull(node, "firstName"))
                .lastName(getTextOrNull(node, "lastName"))
                .departmentName(getTextOrNull(node, "department"))
                .organizationName(getTextOrNull(node, "organizationName"))
                .organizationDomain(getTextOrNull(node, "organizationDomain"))
                .phoneNumber(getTextOrNull(node, "phoneNumber"))
                .countryName(getTextOrNull(node, "countryName"))
                .riskGroup(parseRiskGroup(getTextOrNull(node, "riskGroup")))
                .active(ACTIVE_STATUS.equalsIgnoreCase(getTextOrNull(node, "status")))
                .isRiskProfileExist(getBooleanOrNull(node, "isRiskProfileExist"))
                .build();
    }

    private static RiskGroup parseRiskGroup(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RiskGroup.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Fetch ClientProduct assignment detail by id (product-package assignment).
     * GET /client/admin/product/package/detail/{id}
     */
    public ClientProductDetailDto getProductPackageDetail(String productPackageId) {
        if (productPackageId == null || productPackageId.isBlank()) {
            return null;
        }
        String url = registrationUrl + "/client/admin/product/package/detail/" + productPackageId.trim();
        log.info("Fetching client product package detail: {}", productPackageId);
        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null || response.get("data").isNull()) {
                log.warn("No product package detail returned for id={}", productPackageId);
                return null;
            }
            JsonNode data = response.get("data");
            return ClientProductDetailDto.builder()
                    .id(getTextOrNull(data, "id"))
                    .clientAdminId(getTextOrNull(data, "clientAdminId"))
                    .productId(getTextOrNull(data, "productId"))
                    .packageId(getTextOrNull(data, "packageId"))
                    .licenseCount(getIntOrZero(data, "licenseCount"))
                    .usedLicenseCount(getIntOrZero(data, "usedLicenseCount"))
                    .licenseStatus(getTextOrNull(data, "licenseStatus"))
                    .expiryDate(getInstantOrNull(data, "expiryDate"))
                    .build();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product package not found: {}", productPackageId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching product package detail id={}: {}", productPackageId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sets usedLicenseCount on a ClientProduct assignment by document id (productPackageId).
     * PUT /client/admin/product/package/{id}/used-license-count?usedLicenseCount=
     */
    public void updateClientProductUsedLicenseCount(String productPackageId, int usedLicenseCount) {
        if (productPackageId == null || productPackageId.isBlank()) {
            throw new IllegalArgumentException("productPackageId is required");
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(registrationUrl + "/client/admin/product/package/"
                        + productPackageId.trim() + "/used-license-count")
                .queryParam("usedLicenseCount", usedLicenseCount)
                .toUriString();
        log.info("Updating usedLicenseCount={} for productPackageId={}", usedLicenseCount, productPackageId);
        try {
            JsonNode response = webClient.put()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (response == null || !response.has("statusCode") || response.get("statusCode").asInt() != 200) {
                String errorMessage = response != null && response.has("message")
                        ? response.get("message").asText()
                        : "Unknown error";
                throw new IllegalStateException(
                        "Failed to update used license count in registration: " + errorMessage);
            }
            log.info("Used license count updated successfully for productPackageId={}", productPackageId);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating used license count for productPackageId={}: {}",
                    productPackageId, e.getMessage(), e);
            throw e;
        }
    }

    private int getIntOrZero(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return 0;
        }
        return fieldNode.asInt(0);
    }

    private Instant getInstantOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isTextual()) {
            try {
                return Instant.parse(fieldNode.asText());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    private Boolean getBooleanOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return false;
        if (fieldNode.isBoolean()) return fieldNode.asBoolean();
        return false;
    }

    private static class FetchResult {
        final List<UserDto> users;
        final long total;

        FetchResult(List<UserDto> users, long total) {
            this.users = users;
            this.total = total;
        }
    }

    // --- DTOs for inter-service communication ---

    public record RegistrationTimezone(String displayName, String ianaZoneId) {
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserDto {
        private String userId;
        private String email;
        private String firstName;
        private String lastName;
        private String departmentId;
        private String departmentName;
        private String organizationName;
        private String organizationDomain;
        private String phoneNumber;
        private String countryName;
        private List<String> groupIds;
        private RiskGroup riskGroup;
        private boolean active;
        private Boolean isRiskProfileExist;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ClientProductDetailDto {
        private String id;
        private String clientAdminId;
        private String productId;
        private String packageId;
        private int licenseCount;
        private int usedLicenseCount;
        private String licenseStatus;
        private Instant expiryDate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepartmentDto {
        private String departmentId;
        private String departmentName;
        private int userCount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GroupDto {
        private String groupId;
        private String groupName;
        private int userCount;
    }
}
