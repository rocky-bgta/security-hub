package com.aspire.asat.registration.client.service;

import com.aspire.asat.registration.data.clientAdmin.request.OrganizationInfoDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientOnboardingResponseDto;
import com.aspire.asat.registration.data.notification.AttachmentDto;
import com.aspire.asat.registration.data.notification.NotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class NotificationRequestFactory {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    //for welcome email notification request
    public NotificationRequestDto createWelcomeNotificationRequest(
            OrganizationInfoDto org,
            String tempPassword,
            ClientOnboardingResponseDto response,
            String s3ObjectKey) {

        // Create the attachment details DTO
        AttachmentDto attachment = AttachmentDto.builder()
                .bucketName(bucketName)
                .objectKey(s3ObjectKey)
                .build();


        // Create the model for the Thymeleaf template
        Map<String, Object> templateModel = Map.of(
                "userName", org.getAdminEmail(),
                "password", tempPassword,
                "signupDate", DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                        .withZone(ZoneId.systemDefault()).format(Instant.now()),
                "lmsName", "AspireLMS"
        );

        // Build the full notification request DTO
        return NotificationRequestDto.builder()
                .to(org.getAdminEmail())
                .subject("Welcome to AspireLMS! Your Account Details")
                .templateId("welcome-email")
                .templateModel(templateModel)
                .attachments(List.of(attachment))
                .build();
    }

    //do for other notification requests
}
