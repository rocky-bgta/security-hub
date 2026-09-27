package com.aspire.asat.breachdetection.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RegistrationServiceClient {
    private static final int FETCH_PAGE_SIZE = 1000;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RegistrationServiceClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Value("${service.registration.url}")
    private String registrationUrl;

    public List<UserDto> getAllUsers(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return Collections.emptyList();
        }
        return fetchUsersPage(clientId, 0, FETCH_PAGE_SIZE);

    }


    private List<UserDto> fetchTestUser(String clientId) {
        return List.of(UserDto.builder()
                .userId("superadmin01")
                .email("superadmin01@yopmail.com")
                .firstName("Super")
                .lastName("Admin")
                .build());

    }


    private List<UserDto> fetchUsersPage(String clientId, int offset, int pageSize) {
        String url = UriComponentsBuilder.fromHttpUrl(registrationUrl + "/end-user/users")
                .queryParam("clientAdminId", clientId)
                .queryParam("offset", offset)
                .queryParam("pageSize", pageSize)
                .toUriString();
        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (response == null || response.get("data") == null || response.get("data").get("items") == null) {
                return Collections.emptyList();
            }
            List<JsonNode> rawUsers = objectMapper.readerFor(new TypeReference<List<JsonNode>>() {
            }).readValue(response.get("data").get("items"));
            return rawUsers.stream().map(this::mapToUserDto).collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            log.error("Error calling registration service for client {}: {}", clientId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private UserDto mapToUserDto(JsonNode node) {
        return UserDto.builder()
                .userId(getTextOrNull(node, "id"))
                .email(getTextOrNull(node, "email"))
                .firstName(getTextOrNull(node, "firstName"))
                .lastName(getTextOrNull(node, "lastName"))
                .build();
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String userId;
        private String email;
        private String firstName;
        private String lastName;
    }
}
