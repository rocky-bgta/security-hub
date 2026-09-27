package com.aspire.asat.notification.controller;

import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.dto.dispatch.NotificationDispatchResultDto;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Interface for notification controller
 * Handles email notifications and multi-channel notification sending
 * Also includes TwiML webhook endpoint for Twilio phone calls (internal use only)
 */
@Tag(name = "Legacy Notification", description = "Legacy APIs for email notifications")
@RequestMapping(value = WebApiUrlConstants.NOTIFICATION_API, produces = "application/json")
public interface NotificationController {

    @PostMapping(value = WebApiUrlConstants.NOTIFICATION_SENDING_PATH)
    @Operation(summary = "Send email notification", description = "Send an email notification using the legacy email DTO")
    ResponseEntity<ApiResponseDto<EmailDto>> sendEmail(@RequestBody EmailDto emailDto);

    @PostMapping("/send-notification")
    @Operation(summary = "Send notification", description = "Send a notification through multiple channels. " +
            "For phone call OTP, include 'PHONE_CALL' in channels array and provide OTP in templateModel. " +
            "TwiML is passed directly to Twilio (no webhook required).")
    ResponseEntity<ApiResponseDto<NotificationRequestDto>> sendNotification(@RequestBody NotificationRequestDto requestDto);

    @PostMapping("/send-role-based-notification")
    @Operation(summary = "Send a role-based notification event", description = "Fans a single event out to every applicable " +
            "recipient role (User, Client Admin, MSP, Aspire Admin) after resolving the recipient hierarchy from the " +
            "registration service and applying the global -> role -> org -> user gate chain per recipient.")
    ResponseEntity<ApiResponseDto<NotificationDispatchResultDto>> sendRoleBasedNotification(
            @Valid @RequestBody NotificationEventRequestDto eventRequestDto);
}