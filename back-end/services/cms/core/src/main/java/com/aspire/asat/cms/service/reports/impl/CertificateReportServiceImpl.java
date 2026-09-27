package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.ExpiringCertificateResponseDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.custom.UserCertificateRepositoryCustom;
import com.aspire.asat.cms.service.reports.CertificateReportService;
import com.aspire.asat.cms.util.CertificateStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateReportServiceImpl implements CertificateReportService {

    private static final int DEFAULT_EXPIRY_THRESHOLD_DAYS = 60;
    private static final int EXPIRING_SOON_DAYS = 30;
    private static final String DATE_FIELD_EXPIRY = "expiryDate";
    private static final String DATE_FIELD_ISSUED = "createdAt";
    private static final String ISSUED_BY_SYSTEM = "System";

    private final UserCertificateRepository userCertificateRepository;
    private final UserCertificateRepositoryCustom userCertificateRepositoryCustom;
    private final UserCurrentContextService userCurrentContextService;
    private final ClientAdminServiceClient clientAdminServiceClient;
    private final CmsNotificationClient cmsNotificationClient;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${certificate.expiring-threshold-days:60}")
    private Integer reportThresholdDays;

    @Override
    public Page<ExpiringCertificateResponseDTO> getExpiringCertificates(
            String clientAdminId, String mspId, int offset, int pageSize) {
        log.info("Getting expiring certificates - clientAdminId: {}, mspId: {}, offset: {}, pageSize: {}",
                clientAdminId, mspId, offset, pageSize);

        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, DATE_FIELD_EXPIRY));
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        Instant now = Instant.now();
        Instant expiryThreshold = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);

        if (isMspRequest(context, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(context, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            Page<UserCertificate> certificatesPage = userCertificateRepositoryCustom.findCertificatesForReportByClientAdminIds(
                    clientAdminIds, null, now, expiryThreshold, null, null, null, DATE_FIELD_EXPIRY, pageable);
            return mapToExpiringCertificatePage(certificatesPage, pageable);
        }

        String scopedClientAdminId = resolveClientAdminScope(context, normalize(clientAdminId));
        Page<UserCertificate> certificatesPage = userCertificateRepositoryCustom.findCertificatesForReport(
                scopedClientAdminId, null, now, expiryThreshold, null, null, null, DATE_FIELD_EXPIRY, pageable);
        return mapToExpiringCertificatePage(certificatesPage, pageable);
    }

    @Override
    public void exportCertificatesToExcelAndEmail(String adminId, List<String> certificateIds) {
        log.info("Exporting certificates to Excel and sending email - adminId: {}, certificateIds count: {}",
                adminId, certificateIds != null ? certificateIds.size() : 0);

        try {
            if (adminId == null || adminId.isBlank()) {
                throw new IllegalArgumentException("Client admin ID cannot be null or empty");
            }
            if (certificateIds == null || certificateIds.isEmpty()) {
                throw new IllegalArgumentException("Certificate IDs list cannot be null or empty");
            }

            List<UserCertificate> certificates = resolveCertificatesByIds(certificateIds);
            if (certificates.isEmpty()) {
                throw new ResourceNotFoundException("No certificates found for the provided IDs");
            }

            List<UserCertificate> filteredCertificates = certificates.stream()
                    .filter(cert -> adminId.equals(cert.getClientAdminId()))
                    .toList();

            if (filteredCertificates.isEmpty()) {
                throw new ResourceNotFoundException("No certificates found for adminId: " + adminId);
            }

            ByteArrayOutputStream excelStream = generateLegacyCertificateExcel(filteredCertificates);
            String fileName = "certificates_export_" + adminId + "_" + UUID.randomUUID() + ".xlsx";
            String s3ObjectKey = uploadExcelToS3(excelStream.toByteArray(), fileName);

            Map<String, Map<String, Object>> clientAdminMap = clientAdminServiceClient.fetchAndCacheAllClientAdmins(false);
            Map<String, Object> clientAdmin = clientAdminMap.get(adminId);
            if (clientAdmin == null) {
                throw new ResourceNotFoundException("Client admin not found for adminId: " + adminId);
            }

            String clientAdminEmail = (String) clientAdmin.get("email");
            if (clientAdminEmail == null || clientAdminEmail.isBlank()) {
                throw new ResourceNotFoundException("Client admin email not found for adminId: " + adminId);
            }

            AttachmentDto attachment = AttachmentDto.builder()
                    .bucketName(s3BucketName)
                    .objectKey(s3ObjectKey)
                    .build();

            String adminName = clientAdmin.get("organizationName") != null
                    ? clientAdmin.get("organizationName").toString() : "Client Admin";
            String companyName = clientAdmin.get("organizationName") != null
                    ? clientAdmin.get("organizationName").toString() : "Aspire Tech";

            boolean sent = cmsNotificationClient.sendCertificateExpiringNotification(
                    adminId, clientAdminEmail, adminName, companyName, filteredCertificates.size(), attachment);
            if (!sent) {
                throw new RuntimeException("Failed to send certificate export email");
            }
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to export certificates to Excel and send email - adminId: {}, Error: {}",
                    adminId, e.getMessage(), e);
            throw new RuntimeException("Failed to export certificates: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportCertificatesToExcel(String adminId, String mspId, Integer offset, Integer pageSize) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        int offsetValue = offset != null ? offset : 0;
        int pageSizeValue = pageSize != null && pageSize > 0 ? pageSize : 10;
        Instant now = Instant.now();
        Instant expiryThreshold = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(offsetValue, pageSizeValue, Sort.by(Sort.Direction.ASC, DATE_FIELD_EXPIRY));

        List<UserCertificate> certificates;
        if (isMspRequest(context, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(context, adminId, mspId);
            if (clientAdminIds.isEmpty()) {
                throw new ResourceNotFoundException("No client admins found for the MSP");
            }
            certificates = userCertificateRepositoryCustom.findCertificatesForReportByClientAdminIds(
                    clientAdminIds, null, now, expiryThreshold, null, null, null, DATE_FIELD_EXPIRY, pageable)
                    .getContent();
        } else {
            String scopedClientAdminId = resolveClientAdminScope(context, normalize(adminId));
            certificates = userCertificateRepositoryCustom.findCertificatesForReport(
                    scopedClientAdminId, null, now, expiryThreshold, null, null, null, DATE_FIELD_EXPIRY, pageable)
                    .getContent();
        }

        if (certificates.isEmpty()) {
            throw new ResourceNotFoundException("No certificates found matching the filters");
        }
        try {
            return generateLegacyCertificateExcel(certificates).toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate excel", e);
        }
    }

    @Override
    public ExpiredCertificateReportSummaryDTO getExpiredCertificateReportSummary(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, Integer thresholdDays) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
        int effectiveThresholdDays = getEffectiveThresholdDays(thresholdDays);

        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return emptyExpiredCertificateReportSummary();
            }
            return userCertificateRepositoryCustom.getCertificateReportSummaryStatsForClientAdminIds(
                    clientAdminIds, fromDate, toDate, effectiveThresholdDays, null);
        }

        String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
        return userCertificateRepositoryCustom.getCertificateReportSummaryStats(
                scopedClientAdminId, fromDate, toDate, effectiveThresholdDays, null);
    }

    @Override
    public Page<ExpiredCertificateReportRowDTO> getExpiredCertificateReportRows(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, Integer thresholdDays, int offset, int pageSize) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
        int effectiveThresholdDays = getEffectiveThresholdDays(thresholdDays);
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, DATE_FIELD_EXPIRY));

        Page<UserCertificate> certificatesPage;
        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            certificatesPage = userCertificateRepositoryCustom.findCertificatesForReportByClientAdminIds(
                    clientAdminIds, null, fromDate, toDate, search, status, effectiveThresholdDays,
                    DATE_FIELD_EXPIRY, pageable);
        } else {
            String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
            certificatesPage = userCertificateRepositoryCustom.findCertificatesForReport(
                    scopedClientAdminId, null, fromDate, toDate, search, status, effectiveThresholdDays,
                    DATE_FIELD_EXPIRY, pageable);
        }

        List<ExpiredCertificateReportRowDTO> items = certificatesPage.getContent().stream()
                .map(certificate -> mapToExpiredReportRow(certificate, effectiveThresholdDays))
                .toList();
        return new PageImpl<>(items, pageable, certificatesPage.getTotalElements());
    }

    @Override
    public byte[] exportExpiredCertificateReportToExcel(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, Integer thresholdDays) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
        int effectiveThresholdDays = getEffectiveThresholdDays(thresholdDays);

        List<UserCertificate> certificates;
        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                throw new ResourceNotFoundException("No client admins found for the MSP");
            }
            certificates = userCertificateRepositoryCustom.findCertificatesForReportExportByClientAdminIds(
                    clientAdminIds, null, fromDate, toDate, search, status, effectiveThresholdDays, DATE_FIELD_EXPIRY);
        } else {
            String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
            certificates = userCertificateRepositoryCustom.findCertificatesForReportExport(
                    scopedClientAdminId, null, fromDate, toDate, search, status, effectiveThresholdDays, DATE_FIELD_EXPIRY);
        }

        if (certificates.isEmpty()) {
            throw new ResourceNotFoundException("No certificates found matching the filters");
        }
        try {
            return generateExpiredReportExcel(certificates, effectiveThresholdDays).toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate expired certificate report excel", e);
        }
    }

    @Override
    public IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummary(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();

        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return emptyIssuedCertificateReportSummary();
            }
            return userCertificateRepositoryCustom.getIssuedCertificateReportSummaryStatsForClientAdminIds(
                    clientAdminIds, fromDate, toDate);
        }

        String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
        return userCertificateRepositoryCustom.getIssuedCertificateReportSummaryStats(
                scopedClientAdminId, fromDate, toDate);
    }

    @Override
    public Page<IssuedCertificateReportRowDTO> getIssuedCertificateReportRows(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, int offset, int pageSize) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, DATE_FIELD_ISSUED));

        Page<UserCertificate> certificatesPage;
        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            certificatesPage = userCertificateRepositoryCustom.findCertificatesForReportByClientAdminIds(
                    clientAdminIds, null, fromDate, toDate, search, status, CertificateStatusUtil.EXPIRING_SOON_DAYS,
                    DATE_FIELD_ISSUED, pageable);
        } else {
            String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
            certificatesPage = userCertificateRepositoryCustom.findCertificatesForReport(
                    scopedClientAdminId, null, fromDate, toDate, search, status, CertificateStatusUtil.EXPIRING_SOON_DAYS,
                    DATE_FIELD_ISSUED, pageable);
        }

        List<IssuedCertificateReportRowDTO> items = certificatesPage.getContent().stream()
                .map(this::mapToIssuedReportRow)
                .toList();
        return new PageImpl<>(items, pageable, certificatesPage.getTotalElements());
    }

    @Override
    public byte[] exportIssuedCertificateReportToExcel(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();

        List<UserCertificate> certificates;
        if (isMspRequest(currentUserContext, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(currentUserContext, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                throw new ResourceNotFoundException("No client admins found for the MSP");
            }
            certificates = userCertificateRepositoryCustom.findCertificatesForReportExportByClientAdminIds(
                    clientAdminIds, null, fromDate, toDate, search, status,
                    CertificateStatusUtil.EXPIRING_SOON_DAYS, DATE_FIELD_ISSUED);
        } else {
            String scopedClientAdminId = resolveClientAdminScope(currentUserContext, clientAdminId);
            certificates = userCertificateRepositoryCustom.findCertificatesForReportExport(
                    scopedClientAdminId, null, fromDate, toDate, search, status,
                    CertificateStatusUtil.EXPIRING_SOON_DAYS, DATE_FIELD_ISSUED);
        }

        if (certificates.isEmpty()) {
            throw new ResourceNotFoundException("No certificates found matching the filters");
        }
        try {
            return generateIssuedReportExcel(certificates).toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate issued certificate report excel", e);
        }
    }

    private List<UserCertificate> resolveCertificatesByIds(List<String> certificateIds) {
        Map<String, UserCertificate> byId = new LinkedHashMap<>();
        for (UserCertificate cert : userCertificateRepository.findByCertificateIdIn(certificateIds)) {
            byId.put(cert.getId(), cert);
        }
        for (UserCertificate cert : userCertificateRepository.findAllById(certificateIds)) {
            byId.putIfAbsent(cert.getId(), cert);
        }
        return new ArrayList<>(byId.values());
    }

    private Page<ExpiringCertificateResponseDTO> mapToExpiringCertificatePage(
            Page<UserCertificate> certificatesPage, Pageable pageable) {
        Map<String, Map<String, Object>> clientAdminMap = clientAdminServiceClient.fetchAndCacheAllClientAdmins(true);
        List<ExpiringCertificateResponseDTO> items = certificatesPage.getContent().stream()
                .map(cert -> mapToExpiringCertificateResponseDTO(cert, clientAdminMap))
                .toList();
        return new PageImpl<>(items, pageable, certificatesPage.getTotalElements());
    }

    private ExpiringCertificateResponseDTO mapToExpiringCertificateResponseDTO(
            UserCertificate cert, Map<String, Map<String, Object>> clientAdminMap) {
        return ExpiringCertificateResponseDTO.builder()
                .learnerName(cert.getFullName())
                .clientAdminId(cert.getClientAdminId())
                .clientAdminName(clientAdminMap.get(cert.getClientAdminId()) != null
                        && clientAdminMap.get(cert.getClientAdminId()).get("organizationName") != null
                        ? clientAdminMap.get(cert.getClientAdminId()).get("organizationName").toString()
                        : "")
                .courseName(cert.getProductName())
                .certificateId(cert.getCertificateId())
                .expiryDate(cert.getExpiryDate())
                .status("Expiring")
                .build();
    }

    private ExpiredCertificateReportRowDTO mapToExpiredReportRow(UserCertificate certificate, int thresholdDays) {
        CertificateStatus status = CertificateStatusUtil.resolveStatus(certificate.getExpiryDate(), thresholdDays);
        return ExpiredCertificateReportRowDTO.builder()
                .certificateId(certificate.getCertificateId())
                .user(certificate.getFullName())
                .course(certificate.getProductName())
                .expiryDate(certificate.getExpiryDate())
                .days(formatDaysRelativeToExpiry(certificate.getExpiryDate()))
                .certificateStatus(CertificateStatusUtil.toDisplayLabel(status))
                .build();
    }

    private IssuedCertificateReportRowDTO mapToIssuedReportRow(UserCertificate certificate) {
        CertificateStatus status = CertificateStatusUtil.resolveStatus(certificate.getExpiryDate());
        return IssuedCertificateReportRowDTO.builder()
                .certificateId(certificate.getCertificateId())
                .user(certificate.getFullName())
                .course(certificate.getProductName())
                .issued(certificate.getCreatedAt())
                .expiry(certificate.getExpiryDate())
                .issuedBy(ISSUED_BY_SYSTEM)
                .status(CertificateStatusUtil.toDisplayLabel(status))
                .build();
    }

    private String formatDaysRelativeToExpiry(Instant expiryDate) {
        if (expiryDate == null) {
            return "N/A";
        }
        long daysDiff = ChronoUnit.DAYS.between(
                Instant.now().truncatedTo(ChronoUnit.DAYS),
                expiryDate.truncatedTo(ChronoUnit.DAYS));
        if (daysDiff < 0) {
            return Math.abs(daysDiff) + "d overdue";
        }
        return daysDiff + "d left";
    }

    private int getEffectiveThresholdDays(Integer thresholdDays) {
        if (thresholdDays != null && thresholdDays > 0) {
            return thresholdDays;
        }
        if (reportThresholdDays != null && reportThresholdDays > 0) {
            return reportThresholdDays;
        }
        return DEFAULT_EXPIRY_THRESHOLD_DAYS;
    }

    private String resolveClientAdminScope(CurrentUserContext context, String requestedClientAdminId) {
        try {
            UserType userType = UserType.fromString(context.getUserType());
            if (UserType.CLIENT_ADMIN.equals(userType)) {
                if (context.getClientAdminId() != null && !context.getClientAdminId().isBlank()) {
                    return context.getClientAdminId();
                }
                // CLIENT_ADMIN org id is typically the same as userId
                return context.getUserId();
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unable to resolve user type for certificate report scope: {}", e.getMessage());
        }
        return requestedClientAdminId;
    }

    private boolean isMspRequest(CurrentUserContext context, String mspId) {
        if (mspId != null && !mspId.isBlank()) {
            return true;
        }
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private List<String> resolveMspClientAdminIds(CurrentUserContext context, String clientAdminId, String mspId) {
        String normalizedClientAdminId = normalize(clientAdminId);
        if (normalizedClientAdminId != null) {
            return List.of(normalizedClientAdminId);
        }

        List<String> contextClientAdminIds = context.getClientAdminIds();
        if (contextClientAdminIds != null && !contextClientAdminIds.isEmpty()) {
            return contextClientAdminIds;
        }

        String effectiveMspId = resolveMspId(context, mspId);
        if (effectiveMspId == null || effectiveMspId.isBlank()) {
            log.warn("Unable to resolve MSP ID for userId={}, userType={}", context.getUserId(), context.getUserType());
            return List.of();
        }
        return clientAdminServiceClient.getClientAdminIdsByMspId(effectiveMspId);
    }

    private String resolveMspId(CurrentUserContext context, String requestMspId) {
        try {
            if (UserType.MSP.equals(UserType.fromString(context.getUserType()))) {
                return context.getUserId();
            }
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        return normalize(requestMspId);
    }

    private String normalize(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private ExpiredCertificateReportSummaryDTO emptyExpiredCertificateReportSummary() {
        return ExpiredCertificateReportSummaryDTO.builder()
                .totalCertificates(0L)
                .totalValidCertificates(0L)
                .totalExpiredCertificates(0L)
                .totalExpiringCertificates(0L)
                .build();
    }

    private IssuedCertificateReportSummaryDTO emptyIssuedCertificateReportSummary() {
        return IssuedCertificateReportSummaryDTO.builder()
                .totalIssued(0L)
                .thisMonth(0L)
                .thisQuarter(0L)
                .build();
    }

    private ByteArrayOutputStream generateLegacyCertificateExcel(List<UserCertificate> certificates) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Certificates");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Learner Name", "Course Name", "Client Admin Name", "Certificate ID", "Expiry Date", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<String, Map<String, Object>> clientAdminMap = clientAdminServiceClient.fetchAndCacheAllClientAdmins(false);
            CellStyle dataStyle = workbook.createCellStyle();
            int rowNum = 1;
            for (UserCertificate cert : certificates) {
                Row row = sheet.createRow(rowNum++);
                Map<String, Object> clientAdmin = clientAdminMap.get(cert.getClientAdminId());
                String clientAdminName = clientAdmin != null && clientAdmin.get("organizationName") != null
                        ? clientAdmin.get("organizationName").toString() : "";
                createCell(row, 0, cert.getFullName(), dataStyle);
                createCell(row, 1, cert.getProductName(), dataStyle);
                createCell(row, 2, clientAdminName, dataStyle);
                createCell(row, 3, cert.getCertificateId(), dataStyle);
                createCell(row, 4, cert.getExpiryDate() != null ? cert.getExpiryDate().toString() : "", dataStyle);
                createCell(row, 5, "Expiring", dataStyle);
            }
            workbook.write(out);
            return out;
        }
    }

    private ByteArrayOutputStream generateExpiredReportExcel(List<UserCertificate> certificates, int thresholdDays)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expired Certificate Report");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Cert ID", "User", "Course", "Expiry Date", "Days", "Certificate Status"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
            CellStyle dataStyle = workbook.createCellStyle();
            int rowNum = 1;
            for (UserCertificate cert : certificates) {
                Row row = sheet.createRow(rowNum++);
                ExpiredCertificateReportRowDTO rowData = mapToExpiredReportRow(cert, thresholdDays);
                createCell(row, 0, rowData.getCertificateId(), dataStyle);
                createCell(row, 1, rowData.getUser(), dataStyle);
                createCell(row, 2, rowData.getCourse(), dataStyle);
                createCell(row, 3, cert.getExpiryDate() != null ? dateFormatter.format(cert.getExpiryDate()) : "", dataStyle);
                createCell(row, 4, rowData.getDays(), dataStyle);
                createCell(row, 5, rowData.getCertificateStatus(), dataStyle);
            }
            workbook.write(out);
            return out;
        }
    }

    private ByteArrayOutputStream generateIssuedReportExcel(List<UserCertificate> certificates) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Issued Certificates");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Cert ID", "User", "Course", "Issued", "Expiry", "Issued By", "Status"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
            CellStyle dataStyle = workbook.createCellStyle();
            int rowNum = 1;
            for (UserCertificate cert : certificates) {
                Row row = sheet.createRow(rowNum++);
                IssuedCertificateReportRowDTO rowData = mapToIssuedReportRow(cert);
                createCell(row, 0, rowData.getCertificateId(), dataStyle);
                createCell(row, 1, rowData.getUser(), dataStyle);
                createCell(row, 2, rowData.getCourse(), dataStyle);
                createCell(row, 3, cert.getCreatedAt() != null ? dateFormatter.format(cert.getCreatedAt()) : "", dataStyle);
                createCell(row, 4, cert.getExpiryDate() != null ? dateFormatter.format(cert.getExpiryDate()) : "", dataStyle);
                createCell(row, 5, rowData.getIssuedBy(), dataStyle);
                createCell(row, 6, rowData.getStatus(), dataStyle);
            }
            workbook.write(out);
            return out;
        }
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String uploadExcelToS3(byte[] excelBytes, String fileName) {
        String objectKey = "certificates/exports/" + fileName;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(objectKey)
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(excelBytes));
            return objectKey;
        } catch (S3Exception e) {
            log.error("Error uploading Excel file to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload Excel file to S3", e);
        }
    }
}
