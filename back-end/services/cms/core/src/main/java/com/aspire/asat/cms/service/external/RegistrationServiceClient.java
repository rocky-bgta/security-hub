package com.aspire.asat.cms.service.external;

import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.common.dto.packages.UserSubPackageResponseDTO;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.registration.url}")
    private String registrationUrl;


    public List<UserSubPackageResponseDTO> getUserAssignedSubPackages(String userId, String status) {
        String url = registrationUrl + "/end-user/" + userId + "/sub-packages";

        // Add status parameter if provided
        if (status != null && !status.trim().isEmpty()) {
            url += "?status=" + status;
        }

        log.info("Calling Registration API to get user sub-packages: {} with status filter: {}", url, status);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration response for user: {} with status: {}", userId, status);
                return List.of();
            }

            JsonNode dataNode = response.get("data");
            List<UserSubPackageResponseDTO> subPackages = objectMapper
                    .readerFor(new TypeReference<List<UserSubPackageResponseDTO>>() {
                    })
                    .readValue(dataNode);

            log.info("Successfully retrieved {} sub-packages for user: {} with status filter: {}",
                    subPackages.size(), userId, status);
            return subPackages;

        } catch (Exception e) {
            log.error("Error calling registration service for user: {} with status: {}", userId, status, e);
            throw new AspireException("Failed to retrieve user sub-packages from registration service");
        }
    }

    public UserDataDto getUserData(String userId) {
        try {
            Optional<UserDataDto> userDataOpt = getUserDataById(userId);
            if (userDataOpt.isPresent()) {
                log.info("User data found for userId: {}, returning data {}", userId, userDataOpt.get());
                return userDataOpt.get();

            } else {
                log.warn("User data not found for userId: {}, proceeding with available data", userId);
            }
        } catch (Exception e) {
            log.error("Error fetching user data from registration service for userId: {}",
                    userId, e);
        }
        return null;
    }


    /**
     * Get user data by userId including client admin information
     *
     * @param userId the user ID
     * @return Optional containing UserDataDto with user and client admin information, or empty if not found
     */
    private Optional<UserDataDto> getUserDataById(String userId) {
        String url = registrationUrl + "/end-user/" + userId + "/user-data";

        log.info("Calling Registration API to get user data: {}", url);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration response for user: {}", userId);
                return Optional.empty();
            }

            JsonNode dataNode = response.get("data");
            UserDataDto userData = objectMapper.treeToValue(dataNode, UserDataDto.class);

            log.info("Successfully retrieved user data for userId: {}", userId);
            return Optional.of(userData);

        } catch (Exception e) {
            log.error("Error calling registration service to get user data for userId: {}", userId, e);
            return Optional.empty();
        }
    }

    /**
     * Fetch AspireUser basic details (email, fullName) for a list of user IDs.
     */
    public List<AspireUserBasicDto> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        String url = registrationUrl + "/end-user/by-ids";
        log.info("Calling Registration API to get users by IDs: count={}", userIds.size());

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userIds", userIds);

            JsonNode response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration response for users-by-ids");
                return List.of();
            }

            List<AspireUserBasicDto> users = objectMapper.convertValue(
                    response.get("data"),
                    new TypeReference<List<AspireUserBasicDto>>() {});
            return users != null ? users : List.of();
        } catch (Exception e) {
            log.error("Error calling registration service for users-by-ids", e);
            return List.of();
        }
    }

    /**
     * Fetch package IDs purchased by an MSP for a given product (from registration {@code msp_products}).
     */
    public List<String> getPurchasedPackageIds(String mspId, String productId) {
        if (!StringUtils.hasText(mspId) || !StringUtils.hasText(productId)) {
            return List.of();
        }

        String url = registrationUrl + "/msp-product/purchased-package-ids"
                + "?mspId=" + mspId
                + "&productId=" + productId;
        log.info("Calling Registration API to get purchased package IDs: {}", url);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration response for purchased package IDs "
                        + "(mspId={}, productId={})", mspId, productId);
                return List.of();
            }

            List<String> packageIds = objectMapper.convertValue(
                    response.get("data"),
                    new TypeReference<List<String>>() {});
            return packageIds != null ? packageIds : List.of();
        } catch (Exception e) {
            log.error("Error calling registration service for purchased package IDs "
                    + "(mspId={}, productId={})", mspId, productId, e);
            return List.of();
        }
    }

    /**
     * Unpaginated USER-type IDs matching search (name/email) and/or department for the given client admins.
     */
    public List<String> findEndUserIds(List<String> clientAdminIds, String search, String department) {
        if (clientAdminIds == null || clientAdminIds.isEmpty()) {
            return List.of();
        }

        try {
            URI uri = buildEndUserIdsUri(clientAdminIds, search, department);
            log.info("Calling Registration API to get end-user IDs: {}", uri);

            JsonNode response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration response for end-user IDs");
                return List.of();
            }

            List<String> ids = objectMapper.convertValue(
                    response.get("data"),
                    new TypeReference<List<String>>() {});
            return ids != null ? ids : List.of();
        } catch (Exception e) {
            log.error("Error calling registration service for end-user IDs", e);
            throw new AspireException("Failed to resolve matching users from registration service");
        }
    }

    URI buildEndUserIdsUri(List<String> clientAdminIds, String search, String department) {
        String base = registrationUrl == null ? "" : registrationUrl.trim().replaceAll("/$", "");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(base + "/end-user/ids");
        for (String clientAdminId : clientAdminIds) {
            if (StringUtils.hasText(clientAdminId)) {
                builder.queryParam("clientAdminId", clientAdminId.trim());
            }
        }
        if (StringUtils.hasText(search)) {
            builder.queryParam("search", search.trim());
        }
        if (StringUtils.hasText(department)) {
            builder.queryParam("departments", department.trim());
        }
        return builder.build().toUri();
    }
}