package com.aspire.asat.notification.controller.impl;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.service.InAppNotificationService;
import com.aspire.asat.notification.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NotificationManagementControllerImplTest {

    @Mock
    private InAppNotificationService inAppNotificationService;

    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private NotificationManagementControllerImpl controller;

    @Test
    void getUsedNotificationTypes_returnsOnlyAdminVisibleTypes() {
        ResponseEntity<ApiResponseDto<List<NotificationType>>> response = controller.getUsedNotificationTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<NotificationType> types = response.getBody().getData();
        assertEquals(NotificationType.adminVisibleTypes().size(), types.size());
        assertTrue(types.contains(NotificationType.WELCOME_EMAIL));
        assertTrue(types.contains(NotificationType.PACKAGE_ASSIGNED_USER));
        assertFalse(types.contains(NotificationType.PACKAGE_ASSIGNED));
        assertFalse(types.contains(NotificationType.USER_PROFILE_UPDATED));
        assertFalse(types.contains(NotificationType.COURSE_COMPLETION));
    }

    @Test
    void getNotificationTypes_returnsAllEnumValues() {
        ResponseEntity<ApiResponseDto<List<NotificationType>>> response = controller.getNotificationTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(NotificationType.values().length, response.getBody().getData().size());
    }
}
