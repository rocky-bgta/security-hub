package com.aspire.asat.notification.controller.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.NotificationManagementController;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.model.InAppNotification;
import com.aspire.asat.notification.service.InAppNotificationService;
import com.aspire.asat.notification.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for notification management operations
 * Handles in-app notifications and notification sending
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationManagementControllerImpl implements NotificationManagementController {

    private final InAppNotificationService inAppNotificationService;
    private final UserCurrentContextService userCurrentContextService;

    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getUserNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        log.info("Retrieving notifications for user: {} with offset: {}, pageSize: {}, search: {}", userId, offset, pageSize, search);

        // Use offset-based pagination directly (no need to calculate page number)
        Page<InAppNotification> notificationPage = inAppNotificationService.getUserNotifications(userId, search, offset, pageSize);
        List<InAppNotification> notifications = notificationPage.getContent();

        AllResponseDto<List<InAppNotification>> paginatedResponse =
                new AllResponseDto<>(offset, pageSize, notificationPage.getTotalElements(), notifications);

        ApiResponseDto<AllResponseDto<List<InAppNotification>>> response = new ApiResponseDto<>(
                "User notifications retrieved successfully", HttpStatus.OK.value(), paginatedResponse);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getUnreadNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        log.info("Retrieving unread notifications for user: {} with offset: {}, pageSize: {}, search: {}", userId, offset, pageSize, search);

        // Use offset-based pagination directly (no need to calculate page number)
        Page<InAppNotification> notificationPage = inAppNotificationService.getUnreadNotifications(userId, search, offset, pageSize);
        List<InAppNotification> notifications = notificationPage.getContent();

        AllResponseDto<List<InAppNotification>> paginatedResponse =
                new AllResponseDto<>(offset, pageSize, notificationPage.getTotalElements(), notifications);

        ApiResponseDto<AllResponseDto<List<InAppNotification>>> response = new ApiResponseDto<>(
                "Unread notifications retrieved successfully", HttpStatus.OK.value(), paginatedResponse);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InAppNotification>>>> getArchivedNotifications(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        log.info("Retrieving archived notifications for user: {} with offset: {}, pageSize: {}, search: {}", userId, offset, pageSize, search);

        Page<InAppNotification> notificationPage = inAppNotificationService.getArchivedNotifications(userId, search, offset, pageSize);
        List<InAppNotification> notifications = notificationPage.getContent();

        AllResponseDto<List<InAppNotification>> paginatedResponse =
                new AllResponseDto<>(offset, pageSize, notificationPage.getTotalElements(), notifications);

        ApiResponseDto<AllResponseDto<List<InAppNotification>>> response = new ApiResponseDto<>(
                "Archived notifications retrieved successfully", HttpStatus.OK.value(), paginatedResponse);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseDto<Long>> getUnreadCount() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        log.info("Retrieving unread count for user: {}", userId);

        Long count = inAppNotificationService.getUnreadCount(userId);

        ApiResponseDto<Long> response = new ApiResponseDto<>(
                "Unread count retrieved successfully", HttpStatus.OK.value(), count);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<ApiResponseDto<String>> markAsRead(String notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        inAppNotificationService.markAsRead(notificationId);

        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Notification marked as read", HttpStatus.OK.value(), "Success");
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<ApiResponseDto<Integer>> markAllAsRead() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        log.info("Marking all notifications as read for user: {}", userId);

        Integer count = inAppNotificationService.markAllAsRead(userId);

        ApiResponseDto<Integer> response = new ApiResponseDto<>(
                "All notifications marked as read", HttpStatus.OK.value(), count);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<ApiResponseDto<List<NotificationType>>> getNotificationTypes() {
        log.info("Retrieving all notification types");

        List<NotificationType> types = List.of(NotificationType.values());

        ApiResponseDto<List<NotificationType>> response = new ApiResponseDto<>(
                "Notification types retrieved successfully", HttpStatus.OK.value(), types);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseDto<List<NotificationType>>> getUsedNotificationTypes() {
        log.info("Retrieving used (admin-visible) notification types");

        List<NotificationType> types = List.copyOf(NotificationType.adminVisibleTypes());

        ApiResponseDto<List<NotificationType>> response = new ApiResponseDto<>(
                "Used notification types retrieved successfully", HttpStatus.OK.value(), types);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseDto<List<NotificationType>>> getPriorityNotificationTypes() {
        log.info("Retrieving priority notification types");

        List<NotificationType> priorityTypes = NotificationType.PRIORITY_TYPES;

        ApiResponseDto<List<NotificationType>> response = new ApiResponseDto<>(
                "Priority notification types retrieved successfully", HttpStatus.OK.value(), priorityTypes);
        return ResponseEntity.ok(response);
    }
}
