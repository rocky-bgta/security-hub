package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.exam.CertificateLinksDTO;
import com.aspire.asat.cms.dto.exam.ExamSubmissionAsyncPayload;
import com.aspire.asat.cms.dto.notification.CertificateNotificationRequest;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ClientDashboardRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.TrainingRiskScoreSyncService;
import com.aspire.asat.cms.service.certificate.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Processes exam submission follow-up (certificate, user package update, dashboard, notification) in background.
 * Must be a separate Spring bean so that @Async is applied via proxy when called from ExamServiceImpl.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmissionAsyncProcessor {

    private static final int CERTIFICATE_VALIDITY_DAYS = 365;

    private final ExamRepository examRepository;
    private final UserSubPackageRepository userSubPackageRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final ClientDashboardRepository clientDashboardRepository;
    private final CmsNotificationClient notificationClient;
    private final CertificateService certificateService;
    private final TrainingRiskScoreSyncService trainingRiskScoreSyncService;

    @Async("taskExecutor")
    public void processAfterSubmission(ExamSubmissionAsyncPayload payload) {
        if (payload == null) {
            log.warn("ExamSubmissionAsyncProcessor received null payload, skipping");
            return;
        }
        log.info("Processing exam submission async for userId={}, examId={}, passed={}",
                payload.getUserId(), payload.getExamId(), payload.isPassed());

        try {
            Exams exam = examRepository.findByExamId(payload.getExamId())
                    .orElse(null);
            UserSubPackage userPackage = userSubPackageRepository
                    .findByUserIdAndSubPackageId(payload.getUserId(), payload.getSubPackageId())
                    .orElse(null);

            if (exam == null || userPackage == null) {
                log.warn("Exam or UserSubPackage not found for async processing, examId={}, userId={}, subPackageId={}",
                        payload.getExamId(), payload.getUserId(), payload.getSubPackageId());
                return;
            }

            if (payload.isPassed()) {
                // 1. Generate certificate first; only run post-processing when generation succeeds
                CertificateGenerationResult result = generateCertificate(userPackage, payload);
                if (result != null && result.links() != null && result.links().getPdfLink() != null && !result.links().getPdfLink().isEmpty()) {
                    CertificateLinksDTO links = result.links();
                    // 2. Post-processing (after certificate generation): update entities, persist, then certificate details, dashboard, notification
                    String previousStatus = userPackage.getStatus();
                    userPackage.setCertificateLink(links.getPdfLink());
                    userPackage.setImageCertificateLink(links.getImageLink());
                    userPackage.setStatus(SubPackageStatus.COMPLETED.name());
                    userPackage.setRiskScoreFromStatus(); // Scenario A: COMPLETED → 0 points
                    exam.setCertificateLink(links.getPdfLink());
                    exam.setImageCertificateLink(links.getImageLink());

                    examRepository.save(exam);
                    userSubPackageRepository.save(userPackage);

                    trainingRiskScoreSyncService.syncTrainingRiskScore(previousStatus, userPackage);
                    saveCertificateDetails(userPackage, links, result.certificateId(), exam.getExamId(), payload);
                    updateDashboard(userPackage.getClientAdminId());
                    sendNotification(payload);
                } else {
                    log.warn("Certificate generation failed or returned empty links; skipping post-processing for userId={}, examId={}",
                            payload.getUserId(), payload.getExamId());
                }
            } else {
                userPackage.setCertificateLink(null);
                userPackage.setImageCertificateLink(null);
                exam.setCertificateLink(null);
                exam.setImageCertificateLink(null);
                userSubPackageRepository.save(userPackage);
                examRepository.save(exam);
            }

            log.info("Async exam submission processing completed for userId={}, examId={}", payload.getUserId(), payload.getExamId());
        } catch (Exception e) {
            log.error("Async exam submission processing failed for userId={}, examId={}", payload.getUserId(), payload.getExamId(), e);
        }
    }

    /**
     * Generates certificate only. Returns links and certificateId when successful, null otherwise.
     * All post-processing (save exam, userPackage, UserCertificate, dashboard, notification) is done after this.
     */
    private CertificateGenerationResult generateCertificate(UserSubPackage userPackage, ExamSubmissionAsyncPayload payload) {
        try {
            String certificateId = generateCertificateId();
            String recipientFullName = payload.getUserFullName() != null ? payload.getUserFullName() : "";
            CertificateLinksDTO links = certificateService.generateCertificate(
                    userPackage.getUserId(),
                    userPackage.getSubPackageName(),
                    userPackage.getSubPackageId(),
                    certificateId,
                    userPackage.getClientAdminId(),
                    recipientFullName,
                    userPackage.getIsTrial());

            if (links != null && links.getPdfLink() != null && !links.getPdfLink().isEmpty()) {
                return new CertificateGenerationResult(links, certificateId);
            }
            log.warn("Certificate generation returned empty links for userId={} packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId());
        } catch (Exception e) {
            log.error("Certificate generation failed for userId={} packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId(), e);
        }
        return null;
    }

    private record CertificateGenerationResult(CertificateLinksDTO links, String certificateId) {}

    private void saveCertificateDetails(UserSubPackage userPackage, CertificateLinksDTO links,
                                        String certificateId, String examId, ExamSubmissionAsyncPayload payload) {
        try {
            UserCertificate userCertificate = UserCertificate.builder()
                    .userId(userPackage.getUserId())
                    .userSubPackageId(userPackage.getId())
                    .clientAdminId(userPackage.getClientAdminId())
                    .username(payload.getUserEmail())
                    .fullName(payload.getUserFullName() != null ? payload.getUserFullName() : "")
                    .productName(links.getProductName())
                    .certificateUrl(links.getPdfLink())
                    .expiryDate(Instant.now().plus(CERTIFICATE_VALIDITY_DAYS, ChronoUnit.DAYS))
                    .createdAt(Instant.now())
                    .subPackageId(userPackage.getSubPackageId())
                    .status(CertificateStatus.VALID.name())
                    .certificateLink(links.getPdfLink())
                    .imageCertificateLink(links.getImageLink())
                    .certificateId(certificateId)
                    .examId(examId)
                    .build();

            userCertificateRepository.save(userCertificate);
            log.info("Certificate details saved to user_certificates for userId={}, packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId());
        } catch (Exception e) {
            log.error("Failed to save certificate details for userId={}, packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId(), e);
        }
    }

    private void updateDashboard(String clientAdminId) {
        try {
            clientDashboardRepository.findByClientAdminId(clientAdminId).ifPresent(dashboard -> {
                dashboard.setTotalCertificate(dashboard.getTotalCertificate() + 1);
                clientDashboardRepository.save(dashboard);
            });
        } catch (Exception e) {
            log.error("Failed to update dashboard for clientAdminId={}", clientAdminId, e);
        }
    }

    private void sendNotification(ExamSubmissionAsyncPayload payload) {
        try {
            if (payload.getUserEmail() == null || payload.getUserEmail().isBlank()) {
                log.warn("Skipping certificate notification: no user email in payload for userId={}", payload.getUserId());
                return;
            }
            String courseTitle = payload.getCourseTitle() != null ? payload.getCourseTitle() : "";
            String issueDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

            boolean sent = notificationClient.sendCertificateIssuedNotification(
                    new CertificateNotificationRequest(
                            payload.getUserEmail(),
                            payload.getUserId(),
                            payload.getUserFullName() != null ? payload.getUserFullName() : "",
                            courseTitle,
                            issueDate,
                            payload.getAdminEmail() != null ? payload.getAdminEmail() : "",
                            payload.getAdminName() != null ? payload.getAdminName() : "",
                            payload.getClientAdminId() != null ? payload.getClientAdminId() : ""
                    ));

            if (sent) {
                log.info("Certificate notification sent for userId={}", payload.getUserId());
            } else {
                log.error("Failed to send certificate notification for userId={}", payload.getUserId());
            }
        } catch (Exception e) {
            log.error("Error sending certificate notification for userId={}", payload.getUserId(), e);
        }
    }

    private static String generateCertificateId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = new Random().nextInt(90000) + 10000;
        return String.format("CRT-%s-%05d", date, seq);
    }
}
