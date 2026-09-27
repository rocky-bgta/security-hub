package com.aspire.asat.common.client.impl;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationPriority;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Implementation of NotificationClient that communicates with the notification service
 * Uses RestTemplate to make HTTP calls to the notification service API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClientImpl implements NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification.service.url:http://asat-notification-service}")
    private String notificationServiceUrl;

    @Value("${notification.service.api-path:/notification/api/v1}")
    private String apiPath;

    @Override
    public boolean sendNotification(NotificationRequestDto requestDto) {
        try {
            String url = notificationServiceUrl + apiPath + "/send-notification";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<NotificationRequestDto> request = new HttpEntity<>(requestDto, headers);
            
            log.info("Sending notification to service: {} with type: {}", url, requestDto.getNotificationType());
            
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Notification sent successfully: {}", success);
            
            return success;
            
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendEmailNotification(String to, NotificationType notificationType, Map<String, Object> templateModel) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .to(to)
                .notificationType(notificationType)
                .channels(List.of(NotificationChannel.EMAIL))
                .templateModel(templateModel)
                .priority(NotificationPriority.NORMAL)
                .build();
        
        return sendNotification(requestDto);
    }

    @Override
    public boolean sendInAppNotification(String userId, NotificationType notificationType, Map<String, Object> templateModel) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .userId(userId)
                .to(userId)
                .notificationType(notificationType)
                .channels(List.of(NotificationChannel.IN_APP))
                .templateModel(templateModel)
                .priority(NotificationPriority.NORMAL)
                .build();
        
        return sendNotification(requestDto);
    }

    @Override
    public boolean sendMultiChannelNotification(String to, String userId, NotificationType notificationType, Map<String, Object> templateModel) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .to(to)
                .userId(userId)
                .notificationType(notificationType)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                .templateModel(templateModel)
                .priority(NotificationPriority.NORMAL)
                .build();
        
        return sendNotification(requestDto);
    }


    @Override
    public boolean sendHighPriorityNotification(String to, String userId, NotificationType notificationType, Map<String, Object> templateModel) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .to(to)
                .userId(userId)
                .notificationType(notificationType)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                .templateModel(templateModel)
                .priority(NotificationPriority.HIGH)
                .build();
        
        return sendNotification(requestDto);
    }
    
    @Override
    public boolean sendMultiChannelNotification(String to, String userId, String clientAdminId, NotificationType notificationType, Map<String, Object> templateModel) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .to(to)
                .userId(userId)
                .clientAdminId(clientAdminId)
                .notificationType(notificationType)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                .templateModel(templateModel)
                .priority(NotificationPriority.NORMAL)
                .build();
        
        return sendNotification(requestDto);
    }
    
    @Override
    public boolean sendCustomChannelNotification(String to, String userId, String clientAdminId, NotificationType notificationType,
                                                 List<NotificationChannel> channels, Map<String, Object> templateModel, List<AttachmentDto> attachments) {
        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .to(to)
                .userId(userId)
                .clientAdminId(clientAdminId)
                .notificationType(notificationType)
                .channels(channels)
                .templateModel(templateModel)
                .priority(NotificationPriority.NORMAL)
                .attachments(attachments)
                .build();
        
        return sendNotification(requestDto);
    }

    @Override
    public boolean sendRoleBasedNotification(NotificationEventRequestDto eventRequestDto) {
        try {
            String url = notificationServiceUrl + apiPath + "/send-role-based-notification";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotificationEventRequestDto> request = new HttpEntity<>(eventRequestDto, headers);

            log.info("Sending role-based notification event to service: {} with type: {}", url, eventRequestDto.getNotificationType());

            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Role-based notification event dispatched: {}", success);

            return success;

        } catch (Exception e) {
            log.error("Failed to send role-based notification event: {}", e.getMessage(), e);
            return false;
        }
    }
}
