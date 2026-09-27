package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportOnboardResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportRowStatus;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUpdateRequestDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUserDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportValidateResponseDto;
import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.dto.notification.BulkImportSummaryDto;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.BulkUserImportSession;
import com.aspire.asat.registration.repository.BulkUserImportSessionRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.BulkUserImportService;
import com.aspire.asat.registration.service.EndUserService;
import com.aspire.asat.registration.service.support.BulkImportFileParser;
import com.aspire.asat.registration.util.EmailValidationUtils;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUserImportServiceImpl implements BulkUserImportService {

    public static final String REASON_INVALID_EMAIL_FORMAT = "invalid_email_format";
    public static final String REASON_INVALID_DOMAIN = "invalid_domain";
    public static final String REASON_DUPLICATE_EMAIL = "duplicate_email";
    public static final String REASON_EMAIL_ALREADY_EXISTS = "email_already_exists";
    public static final String REASON_MISSING_REQUIRED_FIELD = "missing_required_field";
    public static final String REASON_CREATION_FAILED = "creation_failed";

    private static final Duration SESSION_TTL = Duration.ofHours(2);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BulkImportFileParser bulkImportFileParser;
    private final BulkUserImportSessionRepository sessionRepository;
    private final AspireUserService aspireUserService;
    private final EndUserService endUserService;
    private final UserCurrentContextService currentContextService;
    private final RegistrationNotificationClient notificationClient;

    @Override
    public BulkImportValidateResponseDto validateImport(
            MultipartFile file, String clientAdminId, int offset, int pageSize) {
        if (!StringUtils.hasText(clientAdminId)) {
            throw new RegistationValidationException("Client admin ID is required");
        }
        bulkImportFileParser.validateFile(file);

        String companyDomain = resolveClientAdminDomain(clientAdminId.trim());
        List<BulkImportFileParser.ParsedBulkImportRow> parsedRows =
                bulkImportFileParser.parseForValidation(file);

        List<BulkUserImportSession.BulkImportSessionRow> sessionRows = new ArrayList<>();
        for (BulkImportFileParser.ParsedBulkImportRow parsed : parsedRows) {
            sessionRows.add(toSessionRow(parsed));
        }
        revalidateAllRows(sessionRows, clientAdminId.trim(), companyDomain);

        CurrentUserContext context = currentContextService.getCurrentUserContext();
        Instant now = Instant.now();
        BulkUserImportSession session = BulkUserImportSession.builder()
                .clientAdminId(clientAdminId.trim())
                .createdBy(context != null ? context.getUserId() : null)
                .status(BulkUserImportSession.STATUS_VALIDATED)
                .rows(sessionRows)
                .createdAt(now)
                .expiresAt(now.plus(SESSION_TTL))
                .build();
        session = sessionRepository.save(session);

        return buildValidateResponse(session, normalizeOffset(offset), normalizePageSize(pageSize));
    }

    @Override
    public AllResponseDto<List<BulkImportUserDto>> getSessionUsers(
            String sessionId, BulkImportRowStatus status, int offset, int pageSize) {
        BulkUserImportSession session = getActiveSession(sessionId);
        return pageUsers(session, status, normalizeOffset(offset), normalizePageSize(pageSize));
    }

    @Override
    public BulkImportValidateResponseDto updateSessionUsers(
            String sessionId, BulkImportUpdateRequestDto request, int offset, int pageSize) {
        BulkUserImportSession session = getActiveSession(sessionId);
        if (request == null || request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new RegistationValidationException("At least one user update is required");
        }

        Map<Integer, BulkUserImportSession.BulkImportSessionRow> byIndex = session.getRows().stream()
                .collect(Collectors.toMap(BulkUserImportSession.BulkImportSessionRow::getRowIndex, r -> r, (a, b) -> a));

        for (BulkImportUserDto update : request.getUsers()) {
            if (update.getRowIndex() == null) {
                throw new RegistationValidationException("rowIndex is required for each updated user");
            }
            BulkUserImportSession.BulkImportSessionRow row = byIndex.get(update.getRowIndex());
            if (row == null) {
                throw new RegistationValidationException("Unknown rowIndex: " + update.getRowIndex());
            }
            applyUserUpdate(row, update);
        }

        String companyDomain = resolveClientAdminDomain(session.getClientAdminId());
        revalidateAllRows(session.getRows(), session.getClientAdminId(), companyDomain);
        session = sessionRepository.save(session);
        return buildValidateResponse(session, normalizeOffset(offset), normalizePageSize(pageSize));
    }

    @Override
    public BulkImportOnboardResponseDto onboardSession(String sessionId) {
        BulkUserImportSession session = getActiveSession(sessionId);
        session.setStatus(BulkUserImportSession.STATUS_ONBOARDING);
        sessionRepository.save(session);

        String companyDomain = resolveClientAdminDomain(session.getClientAdminId());
        revalidateAllRows(session.getRows(), session.getClientAdminId(), companyDomain);

        List<BulkImportSummaryDto.ImportedUserInfo> importedUsers = new ArrayList<>();
        List<BulkImportSummaryDto.SkippedUserInfo> skippedUsers = new ArrayList<>();
        List<BulkImportOnboardResponseDto.BulkImportFailedUserDto> failedUsers = new ArrayList<>();

        List<BulkUserImportSession.BulkImportSessionRow> validRows = session.getRows().stream()
                .filter(BulkUserImportSession.BulkImportSessionRow::isValid)
                .toList();

        // Still-invalid rows count as failed for the summary
        for (BulkUserImportSession.BulkImportSessionRow row : session.getRows()) {
            if (!row.isValid()) {
                String fullName = buildFullName(row.getFirstName(), row.getLastName());
                String reason = row.getFailureReason() != null ? row.getFailureReason() : REASON_CREATION_FAILED;
                skippedUsers.add(BulkImportSummaryDto.SkippedUserInfo.builder()
                        .email(row.getEmail())
                        .fullName(fullName)
                        .reason(reason)
                        .build());
                failedUsers.add(BulkImportOnboardResponseDto.BulkImportFailedUserDto.builder()
                        .email(row.getEmail())
                        .fullName(fullName)
                        .reason(reason)
                        .build());
            }
        }

        for (BulkUserImportSession.BulkImportSessionRow row : validRows) {
            String fullName = buildFullName(row.getFirstName(), row.getLastName());
            try {
                EndUserRequestDTO request = toEndUserRequest(row, session.getClientAdminId());
                endUserService.addEndUser(request);
                importedUsers.add(BulkImportSummaryDto.ImportedUserInfo.builder()
                        .email(row.getEmail())
                        .fullName(fullName)
                        .phoneNumber(row.getPhoneNumber())
                        .department(row.getDepartment())
                        .build());
            } catch (Exception e) {
                log.warn("Bulk onboard failed for {}: {}", row.getEmail(), e.getMessage());
                skippedUsers.add(BulkImportSummaryDto.SkippedUserInfo.builder()
                        .email(row.getEmail())
                        .fullName(fullName)
                        .reason(REASON_CREATION_FAILED)
                        .build());
                failedUsers.add(BulkImportOnboardResponseDto.BulkImportFailedUserDto.builder()
                        .email(row.getEmail())
                        .fullName(fullName)
                        .reason(REASON_CREATION_FAILED)
                        .build());
            }
        }

        sendSummaryNotification(session.getClientAdminId(), importedUsers, skippedUsers);

        session.setStatus(BulkUserImportSession.STATUS_COMPLETED);
        sessionRepository.save(session);

        int totalUsers = session.getRows().size();
        return BulkImportOnboardResponseDto.builder()
                .totalUsers(totalUsers)
                .successful(importedUsers.size())
                .failed(failedUsers.size())
                .failedUsers(failedUsers)
                .build();
    }

    private void sendSummaryNotification(
            String clientAdminId,
            List<BulkImportSummaryDto.ImportedUserInfo> importedUsers,
            List<BulkImportSummaryDto.SkippedUserInfo> skippedUsers) {
        try {
            CurrentUserContext context = currentContextService.getCurrentUserContext();
            String adminName = aspireUserService.getUserById(UUID.fromString(context.getUserId()))
                    .map(AspireUserDto::getCompanyName)
                    .orElse(context.getFullName());

            BulkImportSummaryDto summaryDto = BulkImportSummaryDto.builder()
                    .adminEmail(context.getEmail())
                    .adminName(adminName)
                    .adminId(context.getUserId())
                    .clientAdminId(context.getClientAdminId() != null ? context.getClientAdminId() : clientAdminId)
                    .totalImported(importedUsers.size())
                    .totalSkipped(skippedUsers.size())
                    .importedUsers(importedUsers)
                    .skippedUsers(skippedUsers)
                    .importDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                    .build();

            notificationClient.sendBulkImportSummaryNotification(summaryDto);
            log.info("Bulk import summary notification sent to admin: {}", context.getEmail());
        } catch (Exception e) {
            log.error("Failed to send bulk import summary notification: {}", e.getMessage(), e);
        }
    }

    private BulkUserImportSession getActiveSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new RegistationValidationException("Import session ID is required");
        }
        BulkUserImportSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Import session not found"));

        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(BulkUserImportSession.STATUS_EXPIRED);
            sessionRepository.save(session);
            throw new RegistationValidationException("Import session has expired. Please re-upload the file.");
        }
        if (BulkUserImportSession.STATUS_COMPLETED.equals(session.getStatus())
                || BulkUserImportSession.STATUS_EXPIRED.equals(session.getStatus())) {
            throw new RegistationValidationException("Import session is no longer available. Please re-upload the file.");
        }

        CurrentUserContext context = currentContextService.getCurrentUserContext();
        if (context != null && StringUtils.hasText(context.getUserId())
                && StringUtils.hasText(session.getCreatedBy())
                && !session.getCreatedBy().equals(context.getUserId())) {
            throw new RegistationValidationException("You are not authorized to access this import session");
        }
        if (context != null && StringUtils.hasText(context.getClientAdminId())
                && StringUtils.hasText(session.getClientAdminId())
                && !session.getClientAdminId().equals(context.getClientAdminId())) {
            throw new RegistationValidationException("You are not authorized to access this import session");
        }
        return session;
    }

    private void revalidateAllRows(
            List<BulkUserImportSession.BulkImportSessionRow> rows,
            String clientAdminId,
            String companyDomain) {
        Map<String, List<BulkUserImportSession.BulkImportSessionRow>> emailToRows = new HashMap<>();
        for (BulkUserImportSession.BulkImportSessionRow row : rows) {
            String emailKey = normalizeEmail(row.getEmail());
            if (emailKey != null) {
                emailToRows.computeIfAbsent(emailKey, k -> new ArrayList<>()).add(row);
            }
        }

        Set<String> existingEmails = new HashSet<>();
        for (String email : emailToRows.keySet()) {
            if (aspireUserService.getUserByEmail(email).isPresent()) {
                existingEmails.add(email);
            }
        }

        for (BulkUserImportSession.BulkImportSessionRow row : rows) {
            Optional<String> reason = validateRow(row, companyDomain, emailToRows, existingEmails);
            if (reason.isPresent()) {
                row.setValid(false);
                row.setFailureReason(reason.get());
            } else {
                row.setValid(true);
                row.setFailureReason(null);
            }
        }
    }

    private Optional<String> validateRow(
            BulkUserImportSession.BulkImportSessionRow row,
            String companyDomain,
            Map<String, List<BulkUserImportSession.BulkImportSessionRow>> emailToRows,
            Set<String> existingEmails) {
        if (!StringUtils.hasText(row.getFirstName())
                || !StringUtils.hasText(row.getEmail())
                || !StringUtils.hasText(row.getPhoneNumber())) {
            return Optional.of(REASON_MISSING_REQUIRED_FIELD);
        }

        String email = row.getEmail().trim();
        if (!EmailValidationUtils.isValidEmail(email)) {
            return Optional.of(REASON_INVALID_EMAIL_FORMAT);
        }

        String domain = extractDomain(email);
        if (domain == null || !domain.equalsIgnoreCase(companyDomain)) {
            return Optional.of(REASON_INVALID_DOMAIN);
        }

        String emailKey = normalizeEmail(email);
        List<BulkUserImportSession.BulkImportSessionRow> sameEmail = emailToRows.getOrDefault(emailKey, List.of());
        if (sameEmail.size() > 1) {
            return Optional.of(REASON_DUPLICATE_EMAIL);
        }

        if (existingEmails.contains(emailKey)) {
            return Optional.of(REASON_EMAIL_ALREADY_EXISTS);
        }

        return Optional.empty();
    }

    private String resolveClientAdminDomain(String clientAdminId) {
        try {
            AspireUserDto clientAdmin = aspireUserService.getUserById(UUID.fromString(clientAdminId))
                    .orElseThrow(() -> new ResourceNotFoundException("Client admin not found with ID: " + clientAdminId));
            if (!StringUtils.hasText(clientAdmin.getEmail())) {
                throw new RegistationValidationException("Client admin email not found");
            }
            String domain = extractDomain(clientAdmin.getEmail().trim());
            if (domain == null) {
                throw new RegistationValidationException("Invalid client admin email format");
            }
            return domain;
        } catch (IllegalArgumentException e) {
            throw new RegistationValidationException("Invalid client admin ID format");
        }
    }

    private static String extractDomain(String email) {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return null;
        }
        String domain = email.substring(atIndex + 1).trim().toLowerCase(Locale.ROOT);
        return domain.isEmpty() ? null : domain;
    }

    private static String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BulkUserImportSession.BulkImportSessionRow toSessionRow(BulkImportFileParser.ParsedBulkImportRow parsed) {
        return BulkUserImportSession.BulkImportSessionRow.builder()
                .rowIndex(parsed.getRowIndex())
                .firstName(parsed.getFirstName())
                .lastName(parsed.getLastName())
                .email(parsed.getEmail())
                .phoneNumber(parsed.getPhoneNumber())
                .countryCode(parsed.getCountryCode())
                .department(parsed.getDepartment())
                .valid(false)
                .build();
    }

    private void applyUserUpdate(BulkUserImportSession.BulkImportSessionRow row, BulkImportUserDto update) {
        if (update.getFirstName() != null) {
            row.setFirstName(update.getFirstName().trim());
        }
        if (update.getLastName() != null) {
            row.setLastName(update.getLastName().trim());
        }
        if (update.getEmail() != null) {
            row.setEmail(update.getEmail().trim());
        }
        if (update.getPhoneNumber() != null) {
            row.setPhoneNumber(update.getPhoneNumber().trim());
        }
        if (update.getPhoneCode() != null) {
            row.setPhoneCode(update.getPhoneCode().trim());
        }
        if (update.getCountryCode() != null) {
            row.setCountryCode(update.getCountryCode().trim());
        }
        if (update.getDepartment() != null) {
            row.setDepartment(update.getDepartment().trim());
        }
    }

    private EndUserRequestDTO toEndUserRequest(BulkUserImportSession.BulkImportSessionRow row, String clientAdminId) {
        EndUserRequestDTO dto = new EndUserRequestDTO();
        dto.setFirstName(row.getFirstName());
        dto.setLastName(row.getLastName() != null ? row.getLastName() : "");
        dto.setEmail(row.getEmail());
        dto.setPhoneNumber(row.getPhoneNumber());
        dto.setPhoneCode(row.getPhoneCode());
        dto.setCountryCode(row.getCountryCode() != null ? row.getCountryCode() : "");
        dto.setDepartment(row.getDepartment() != null ? row.getDepartment() : "");
        dto.setClientAdminId(clientAdminId);
        dto.setStatus(UserStatus.ACTIVE);
        return dto;
    }

    private BulkImportValidateResponseDto buildValidateResponse(
            BulkUserImportSession session, int offset, int pageSize) {
        return BulkImportValidateResponseDto.builder()
                .importSessionId(session.getId())
                .totalValid(session.getRows().stream().filter(BulkUserImportSession.BulkImportSessionRow::isValid).count())
                .totalInvalid(session.getRows().stream().filter(r -> !r.isValid()).count())
                .validUsers(pageUsers(session, BulkImportRowStatus.VALID, offset, pageSize))
                .invalidUsers(pageUsers(session, BulkImportRowStatus.INVALID, offset, pageSize))
                .build();
    }

    private AllResponseDto<List<BulkImportUserDto>> pageUsers(
            BulkUserImportSession session, BulkImportRowStatus status, int offset, int pageSize) {
        List<BulkUserImportSession.BulkImportSessionRow> filtered = session.getRows().stream()
                .filter(r -> status == BulkImportRowStatus.VALID ? r.isValid() : !r.isValid())
                .toList();
        long total = filtered.size();
        List<BulkImportUserDto> items = filtered.stream()
                .skip(offset)
                .limit(pageSize)
                .map(this::toUserDto)
                .toList();
        return new AllResponseDto<>(offset, pageSize, total, items);
    }

    private BulkImportUserDto toUserDto(BulkUserImportSession.BulkImportSessionRow row) {
        return BulkImportUserDto.builder()
                .rowIndex(row.getRowIndex())
                .firstName(row.getFirstName())
                .lastName(row.getLastName())
                .fullName(buildFullName(row.getFirstName(), row.getLastName()))
                .email(row.getEmail())
                .phoneNumber(row.getPhoneNumber())
                .phoneCode(row.getPhoneCode())
                .countryCode(row.getCountryCode())
                .department(row.getDepartment())
                .failureReason(row.isValid() ? null : row.getFailureReason())
                .build();
    }

    private static String buildFullName(String firstName, String lastName) {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        if (last.isEmpty()) {
            return first;
        }
        if (first.isEmpty()) {
            return last;
        }
        return first + " " + last;
    }

    private static int normalizeOffset(int offset) {
        return Math.max(offset, 0);
    }

    private static int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, 100);
    }
}
