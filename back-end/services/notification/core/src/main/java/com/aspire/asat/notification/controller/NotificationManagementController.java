package com.aspire.asat.notification.controller;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.model.InAppNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Interface for notification management controller
 * Handles in-app notifications and notification type management
 */
@Tag(name = "Notification Management", description = "APIs for managing notifications and in-app notifications")
@RequestMapping(WebApiUrlConstants.NOTIFICATION_MANAGEMENT_API)
public interface NotificationManagementController {

    @GetMapping("/in-app")
    @Operation(summary = "Get user notifications", description = "Get all in-app notifications for the current user with pagination and search")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getUserNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search);

    @GetMapping("/in-app/unread")
    @Operation(summary = "Get unread notifications", description = "Get unread in-app notifications for the current user with pagination and search")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getUnreadNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search);

    @GetMapping("/in-app/archived")
    @Operation(summary = "Get archived notifications", description = "Get archived (read) in-app notifications for the current user with pagination and search")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getArchivedNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search);

    @GetMapping("/in-app/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications for the current user")
    ResponseEntity<ApiResponseDto<Long>> getUnreadCount();

    @PutMapping("/in-app/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    ResponseEntity<ApiResponseDto<String>> markAsRead(@PathVariable String notificationId);

    @PutMapping("/in-app/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the current user")
    ResponseEntity<ApiResponseDto<Integer>> markAllAsRead();

    @GetMapping("/types")
    @Operation(summary = "Get notification types", description = "Get all available notification types")
    ResponseEntity<ApiResponseDto<List<NotificationType>>> getNotificationTypes();

    @GetMapping("/types/used")
    @Operation(summary = "Get used notification types",
               description = "Get notification types that are actively used and exposed in the admin role-settings and template APIs")
    ResponseEntity<ApiResponseDto<List<NotificationType>>> getUsedNotificationTypes();

    @GetMapping("/types/priority")
    @Operation(summary = "Get priority notification types", description = "Get priority notification types")
    ResponseEntity<ApiResponseDto<List<NotificationType>>> getPriorityNotificationTypes();
}