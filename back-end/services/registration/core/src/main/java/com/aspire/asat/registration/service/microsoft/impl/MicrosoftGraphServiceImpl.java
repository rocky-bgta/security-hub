package com.aspire.asat.registration.service.microsoft.impl;

import com.aspire.asat.registration.data.microsoft.MicrosoftGroupDto;
import com.aspire.asat.registration.data.microsoft.MicrosoftUserDto;
import com.aspire.asat.registration.service.microsoft.MicrosoftGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MicrosoftGraphServiceImpl implements MicrosoftGraphService {

    private final RestTemplate restTemplate;
    private static final String GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0";

    @Override
    public List<MicrosoftGroupDto> getAllGroups(String accessToken) {
        log.info("Fetching all groups using customer's access token");

        String url = GRAPH_BASE_URL + "/groups?$select=id,displayName,description,mailNickname&$top=100";
        List<MicrosoftGroupDto> groups = new ArrayList<>();

        try {
            Map<String, Object> response = callGraphApi(url, accessToken);
            List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
            if (value != null) {
                for (Map<String, Object> group : value) {
                    groups.add(mapToGroupDto(group));
                }
            }
            log.info("Fetched {} groups", groups.size());
        } catch (Exception e) {
            log.error("Error fetching groups", e);
            throw new RuntimeException("Failed to fetch groups: " + e.getMessage());
        }

        return groups;
    }

    @Override
    public MicrosoftGroupDto getGroupById(String groupId, String accessToken) {
        log.info("Fetching group by ID: {}", groupId);

        String url = GRAPH_BASE_URL + "/groups/" + groupId + "?$select=id,displayName,description,mailNickname";

        try {
            Map<String, Object> response = callGraphApi(url, accessToken);
            return mapToGroupDto(response);
        } catch (Exception e) {
            log.error("Error fetching group by ID: {}", groupId, e);
            throw new RuntimeException("Failed to fetch group: " + e.getMessage());
        }
    }

    @Override
    public List<MicrosoftUserDto> getGroupMembers(String groupId, String accessToken) {
        log.info("Fetching members of group: {}", groupId);

        String url = GRAPH_BASE_URL + "/groups/" + groupId + "/members?$select=id,displayName,mail,userPrincipalName,givenName,surname,jobTitle,department,officeLocation,mobilePhone,accountEnabled&$top=999";
        List<MicrosoftUserDto> members = new ArrayList<>();

        try {
            Map<String, Object> response = callGraphApi(url, accessToken);
            List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
            if (value != null) {
                for (Map<String, Object> member : value) {
                    String odataType = (String) member.get("@odata.type");
                    if ("#microsoft.graph.user".equals(odataType)) {
                        members.add(mapToUserDto(member));
                    }
                }
            }
            log.info("Fetched {} members from group: {}", members.size(), groupId);
        } catch (Exception e) {
            log.error("Error fetching group members", e);
            throw new RuntimeException("Failed to fetch group members: " + e.getMessage());
        }

        return members;
    }

    @Override
    public List<MicrosoftUserDto> getAllUsers(String accessToken) {
        log.info("Fetching all users using customer's access token");

        String url = GRAPH_BASE_URL + "/users?$select=id,displayName,mail,userPrincipalName,givenName,surname,jobTitle,department,officeLocation,mobilePhone,accountEnabled&$top=999";
        List<MicrosoftUserDto> users = new ArrayList<>();

        try {
            Map<String, Object> response = callGraphApi(url, accessToken);
            List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
            if (value != null) {
                for (Map<String, Object> user : value) {
                    users.add(mapToUserDto(user));
                }
            }
            log.info("Fetched {} users", users.size());
        } catch (Exception e) {
            log.error("Error fetching users", e);
            throw new RuntimeException("Failed to fetch users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public MicrosoftUserDto getUserById(String userId, String accessToken) {
        log.info("Fetching user by ID: {}", userId);

        String url = GRAPH_BASE_URL + "/users/" + userId + "?$select=id,displayName,mail,userPrincipalName,givenName,surname,jobTitle,department,officeLocation,mobilePhone,accountEnabled";

        try {
            Map<String, Object> response = callGraphApi(url, accessToken);
            return mapToUserDto(response);
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage());
        }
    }

    private Map<String, Object> callGraphApi(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    private MicrosoftGroupDto mapToGroupDto(Map<String, Object> group) {
        if (group == null) return null;

        return MicrosoftGroupDto.builder()
                .id((String) group.get("id"))
                .displayName((String) group.get("displayName"))
                .description((String) group.get("description"))
                .mailNickname((String) group.get("mailNickname"))
                .build();
    }

    private MicrosoftUserDto mapToUserDto(Map<String, Object> user) {
        if (user == null) return null;

        return MicrosoftUserDto.builder()
                .id((String) user.get("id"))
                .displayName((String) user.get("displayName"))
                .email((String) user.get("mail"))
                .userPrincipalName((String) user.get("userPrincipalName"))
                .givenName((String) user.get("givenName"))
                .surname((String) user.get("surname"))
                .jobTitle((String) user.get("jobTitle"))
                .department((String) user.get("department"))
                .officeLocation((String) user.get("officeLocation"))
                .mobilePhone((String) user.get("mobilePhone"))
                .accountEnabled(Boolean.TRUE.equals(user.get("accountEnabled")))
                .build();
    }

}

