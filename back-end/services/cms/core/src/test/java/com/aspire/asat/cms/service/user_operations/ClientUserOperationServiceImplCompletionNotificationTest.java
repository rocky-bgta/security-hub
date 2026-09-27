package com.aspire.asat.cms.service.user_operations;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.notification.CourseCompletionNotificationRequest;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.CourseRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.PackageRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserContentStatusRepository;
import com.aspire.asat.cms.repository.UserCourseProgressRepository;
import com.aspire.asat.cms.repository.UserCourseRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.UserTopicProgressRepository;
import com.aspire.asat.cms.repository.custom.CourseRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.TrainingRiskScoreSyncService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.util.AzureCertificateUploader;
import com.aspire.asat.cms.util.CertificateGenerator;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.UserDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientUserOperationServiceImplCompletionNotificationTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private WebClient webClient;
    @Mock private CourseRepository courseRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private UserContentStatusRepository userContentStatusRepository;
    @Mock private UserPackageRepositoryCustom userPackageRepositoryCustom;
    @Mock private CourseRepositoryCustom courseRepositoryCustom;
    @Mock private UserCourseRepository userCourseRepository;
    @Mock private UserCourseProgressRepository userCourseProgressRepository;
    @Mock private UserTopicProgressRepository userTopicProgressRepository;
    @Mock private UserSubPackageRepository userSubPackageRepository;
    @Mock private SubPackageRepository subPackageRepository;
    @Mock private ContentRepository contentRepository;
    @Mock private CertificateGenerator certificateGenerator;
    @Mock private AzureCertificateUploader azureCertificateUploader;
    @Mock private PackageRepository packageRepository;
    @Mock private ExamRepository examRepository;
    @Mock private ChapterRepository chapterRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private TrainingRiskScoreSyncService trainingRiskScoreSyncService;
    @Mock private CmsNotificationClient cmsNotificationClient;
    @Mock private RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private ClientUserOperationServiceImpl service;

    @Test
    void shouldSendCourseCompletionNotification_whenInProgress_returnsTrue() {
        assertTrue(ClientUserOperationServiceImpl.shouldSendCourseCompletionNotification("IN_PROGRESS"));
    }

    @Test
    void shouldSendCourseCompletionNotification_whenAlreadyCompleted_returnsFalse() {
        assertFalse(ClientUserOperationServiceImpl.shouldSendCourseCompletionNotification(
                SubPackageStatus.PHISHING_TRAINING_COMPLETED.name()));
    }

    @Test
    void shouldSendCourseCompletionNotification_whenPreviousStatusNull_returnsTrue() {
        assertTrue(ClientUserOperationServiceImpl.shouldSendCourseCompletionNotification(null));
    }

    @Test
    void isPhishingDefaultTrainingSubPackage_whenAssignedForSet_returnsTrue() {
        SubPackage subPackage = SubPackage.builder().assignedFor("PHISHING_TRAINING_FOR_ALL").build();
        assertTrue(service.isPhishingDefaultTrainingSubPackage(subPackage));
    }

    @Test
    void isPhishingDefaultTrainingSubPackage_whenAssignedForMissing_returnsFalse() {
        SubPackage subPackage = SubPackage.builder().build();
        assertFalse(service.isPhishingDefaultTrainingSubPackage(subPackage));
    }

    @Test
    void sendCourseCompletionNotification_invokesNotificationClient() {
        UserDataDto userData = UserDataDto.builder()
                .userId("user-1")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .clientAdminId("admin-1")
                .clientAdminName("Acme Admin")
                .clientAdminEmail("admin@example.com")
                .build();

        when(registrationServiceClient.getUserData("user-1")).thenReturn(userData);
        when(cmsNotificationClient.sendCourseCompletionNotifications(any())).thenReturn(true);

        UserSubPackage userSubPackage = UserSubPackage.builder()
                .userId("user-1")
                .subPackageId("sub-1")
                .subPackageName("Security Awareness Training")
                .clientAdminId("admin-1")
                .userEmail("jane@example.com")
                .build();

        SubPackage subPackage = SubPackage.builder()
                .id("sub-1")
                .name("Security Awareness Training")
                .assignedFor("PHISHING_TRAINING_FOR_ALL")
                .build();

        service.sendCourseCompletionNotification("user-1", userSubPackage, subPackage);

        ArgumentCaptor<CourseCompletionNotificationRequest> captor =
                ArgumentCaptor.forClass(CourseCompletionNotificationRequest.class);
        verify(cmsNotificationClient).sendCourseCompletionNotifications(captor.capture());

        CourseCompletionNotificationRequest request = captor.getValue();
        assertTrue(request.userEmail().equals("jane@example.com"));
        assertTrue(request.userName().equals("Jane Doe"));
        assertTrue(request.courseTitle().equals("Security Awareness Training"));
        assertTrue(request.adminEmail().equals("admin@example.com"));
    }

    @Test
    void sendCourseCompletionNotification_skipsWhenUserEmailMissing() {
        when(registrationServiceClient.getUserData("user-1")).thenReturn(null);

        UserSubPackage userSubPackage = UserSubPackage.builder()
                .userId("user-1")
                .subPackageId("sub-1")
                .build();

        service.sendCourseCompletionNotification("user-1", userSubPackage, SubPackage.builder().build());

        verify(cmsNotificationClient, never()).sendCourseCompletionNotifications(any());
    }
}
