package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.client.responseDto.ExamSubmissionResponseDTO;
import com.aspire.asat.cms.dto.exam.FinalExamSubmissionDto;
import com.aspire.asat.cms.model.ExamQuestionsAttempt;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.repository.ClientDashboardRepository;
import com.aspire.asat.cms.repository.ExamAttemptRepository;
import com.aspire.asat.cms.repository.ExamQuestionRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.NewQuestionRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.mapper.ExamMapper;
import com.aspire.asat.cms.service.certificate.CertificateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplSubmitFinalExamTest {

    private static final String EXAM_ID = "exam-1";
    private static final String USER_ID = "user-1";
    private static final String SUB_PACKAGE_ID = "sub-package-1";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String FULL_NAME = "John Doe";
    private static final String PACKAGE_NAME = "Security Awareness Training";

    @Mock
    private ExamRepository examRepository;
    @Mock
    private CertificateService certificateService;
    @Mock
    private ExamSettingsService examSettingsService;
    @Mock
    private SubPackageRepository subPackageRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private QuestionCustomRepository questionCustomRepository;
    @Mock
    private NewQuestionRepository questionRepository;
    @Mock
    private ExamAttemptRepository examAttemptRepository;
    @Mock
    private ExamQuestionRepository examQuestionRepository;
    @Mock
    private ExamMapper examMapper;
    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private UserCertificateRepository userCertificateRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ClientDashboardRepository clientDashboardRepository;
    @Mock
    private CmsNotificationClient notificationClient;
    @Mock
    private ClientAdminServiceClient clientAdminServiceClient;
    @Mock
    private ExamSubmissionAsyncProcessor examSubmissionAsyncProcessor;

    @InjectMocks
    private ExamServiceImpl examService;

    @Captor
    private ArgumentCaptor<Exams> examCaptor;

    @Test
    void submitFinalExam_setsFullNameAndProductName_whenExamPassed() {
        FinalExamSubmissionDto request = FinalExamSubmissionDto.builder()
                .examId(EXAM_ID)
                .userId(USER_ID)
                .build();
        Exams exam = buildExam();
        List<ExamQuestionsAttempt> attempts = createAttempts(10, 8);

        when(examRepository.findByExamId(EXAM_ID)).thenReturn(Optional.of(exam));
        when(examAttemptRepository.findByExamIdAndUserId(EXAM_ID, USER_ID)).thenReturn(attempts);
        when(examQuestionRepository.countByExamId(EXAM_ID)).thenReturn(10L);
        when(examSettingsService.getExamSettingForClient(CLIENT_ADMIN_ID))
                .thenReturn(ExamSettingsDto.builder().passingScore(75).build());
        when(subPackageRepository.findById(SUB_PACKAGE_ID))
                .thenReturn(Optional.of(SubPackage.builder().id(SUB_PACKAGE_ID).name(PACKAGE_NAME).build()));
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().fullName(FULL_NAME).clientAdminId(CLIENT_ADMIN_ID).build());
        when(examRepository.save(any(Exams.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(examSubmissionAsyncProcessor).processAfterSubmission(any());

        ExamSubmissionResponseDTO response = examService.submitFinalExam(request);

        verify(examRepository).save(examCaptor.capture());
        Exams savedExam = examCaptor.getValue();
        assertEquals(FULL_NAME, savedExam.getFullName());
        assertEquals(PACKAGE_NAME, savedExam.getProductName());
        assertTrue(savedExam.isExamCompleted());
        assertTrue(savedExam.isExamPassed());
        assertEquals(80.0, savedExam.getExamScore());
        assertEquals(PACKAGE_NAME, response.getPackageName());
    }

    @Test
    void submitFinalExam_setsFullNameAndProductName_whenExamFailed() {
        FinalExamSubmissionDto request = FinalExamSubmissionDto.builder()
                .examId(EXAM_ID)
                .userId(USER_ID)
                .build();
        Exams exam = buildExam();
        List<ExamQuestionsAttempt> attempts = createAttempts(10, 3);

        when(examRepository.findByExamId(EXAM_ID)).thenReturn(Optional.of(exam));
        when(examAttemptRepository.findByExamIdAndUserId(EXAM_ID, USER_ID)).thenReturn(attempts);
        when(examQuestionRepository.countByExamId(EXAM_ID)).thenReturn(10L);
        when(examSettingsService.getExamSettingForClient(CLIENT_ADMIN_ID))
                .thenReturn(ExamSettingsDto.builder().passingScore(75).build());
        when(subPackageRepository.findById(SUB_PACKAGE_ID))
                .thenReturn(Optional.of(SubPackage.builder().id(SUB_PACKAGE_ID).name(PACKAGE_NAME).build()));
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().fullName(FULL_NAME).clientAdminId(CLIENT_ADMIN_ID).build());
        when(examRepository.save(any(Exams.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(examSubmissionAsyncProcessor).processAfterSubmission(any());

        examService.submitFinalExam(request);

        verify(examRepository).save(examCaptor.capture());
        Exams savedExam = examCaptor.getValue();
        assertEquals(FULL_NAME, savedExam.getFullName());
        assertEquals(PACKAGE_NAME, savedExam.getProductName());
        assertFalse(savedExam.isExamPassed());
    }

    private Exams buildExam() {
        Exams exam = new Exams();
        exam.setExamId(EXAM_ID);
        exam.setUserId(USER_ID);
        exam.setSubPackageId(SUB_PACKAGE_ID);
        exam.setClientAdminId(CLIENT_ADMIN_ID);
        exam.setExamAttempts(0);
        return exam;
    }

    private static List<ExamQuestionsAttempt> createAttempts(int total, int correctCount) {
        List<ExamQuestionsAttempt> attempts = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            attempts.add(ExamQuestionsAttempt.builder()
                    .examId(EXAM_ID)
                    .userId(USER_ID)
                    .isCorrect(i < correctCount)
                    .build());
        }
        return attempts;
    }
}
