package com.aspire.asat.registration.integration;

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
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.model.BulkUserImportSession;
import com.aspire.asat.registration.repository.BulkUserImportSessionRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.EndUserService;
import com.aspire.asat.registration.service.impl.BulkUserImportServiceImpl;
import com.aspire.asat.registration.service.support.BulkImportFileParser;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Always-on flow integration covering validate → list → update → onboard
 * with a real parser + in-memory session store (no Docker required).
 * Companion to {@link BulkUserImportIntegrationTest} (Testcontainers / Mongo).
 */
@ExtendWith(MockitoExtension.class)
class BulkUserImportFlowIntegrationTest {

    private static final String CLIENT_ADMIN_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String CSV_HEADER = "firstName,lastName,email,phoneNumber,department,countryCode\n";

    private final Map<String, BulkUserImportSession> store = new ConcurrentHashMap<>();

    private AspireUserService aspireUserService;
    private EndUserService endUserService;
    private RegistrationNotificationClient notificationClient;
    private BulkUserImportServiceImpl bulkUserImportService;

    @BeforeEach
    void setUp() {
        store.clear();
        BulkUserImportSessionRepository sessionRepository = mock(BulkUserImportSessionRepository.class);
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> {
            BulkUserImportSession session = inv.getArgument(0);
            if (session.getId() == null || session.getId().isBlank()) {
                session.setId(UUID.randomUUID().toString());
            }
            store.put(session.getId(), copySession(session));
            return session;
        });
        when(sessionRepository.findById(any())).thenAnswer(inv ->
                Optional.ofNullable(store.get(inv.getArgument(0))).map(this::copySession));

        aspireUserService = mock(AspireUserService.class);
        endUserService = mock(EndUserService.class);
        notificationClient = mock(RegistrationNotificationClient.class);
        UserCurrentContextService currentContextService = mock(UserCurrentContextService.class);

        when(currentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .email("admin@company.com")
                .fullName("Admin User")
                .clientAdminId(CLIENT_ADMIN_ID)
                .build());
        when(aspireUserService.getUserById(UUID.fromString(CLIENT_ADMIN_ID)))
                .thenReturn(Optional.of(AspireUserDto.builder()
                        .email("admin@company.com")
                        .companyName("Company")
                        .build()));
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(aspireUserService.getUserByEmail("exists@company.com"))
                .thenReturn(Optional.of(AspireUserDto.builder().email("exists@company.com").build()));
        when(endUserService.addEndUser(any(EndUserRequestDTO.class))).thenAnswer(inv -> {
            EndUserRequestDTO req = inv.getArgument(0);
            return EndUserResponseDTO.builder().email(req.getEmail()).fullName(req.getFirstName()).build();
        });

        bulkUserImportService = new BulkUserImportServiceImpl(
                new BulkImportFileParser(),
                sessionRepository,
                aspireUserService,
                endUserService,
                currentContextService,
                notificationClient);
    }

    @Test
    void fullFlow_ValidateListUpdateOnboard_CompletesAndLocksSession() throws IOException {
        String csv = CSV_HEADER
                + "John,Doe,john@company.com,111,IT,US\n"
                + "Bad,Email,not-an-email,222,IT,US\n"
                + "Exists,User,exists@company.com,333,IT,US\n";

        BulkImportValidateResponseDto validated =
                bulkUserImportService.validateImport(csvFile(csv), CLIENT_ADMIN_ID, 0, 10);

        assertNotNull(validated.getImportSessionId());
        assertEquals(1, validated.getTotalValid());
        assertEquals(2, validated.getTotalInvalid());
        assertTrue(store.containsKey(validated.getImportSessionId()));
        assertEquals(BulkUserImportSession.STATUS_VALIDATED,
                store.get(validated.getImportSessionId()).getStatus());

        AllResponseDto<List<BulkImportUserDto>> invalidPage = bulkUserImportService.getSessionUsers(
                validated.getImportSessionId(), BulkImportRowStatus.INVALID, 0, 10);
        assertEquals(2L, invalidPage.getTotal());

        BulkImportUserDto badRow = invalidPage.getItems().stream()
                .filter(u -> "not-an-email".equals(u.getEmail()))
                .findFirst()
                .orElseThrow();

        BulkImportValidateResponseDto updated = bulkUserImportService.updateSessionUsers(
                validated.getImportSessionId(),
                BulkImportUpdateRequestDto.builder()
                        .users(List.of(BulkImportUserDto.builder()
                                .rowIndex(badRow.getRowIndex())
                                .email("fixed@company.com")
                                .firstName("Bad")
                                .lastName("Email")
                                .phoneNumber("222")
                                .department("IT")
                                .build()))
                        .build(),
                0,
                10);

        assertEquals(2, updated.getTotalValid());
        assertEquals(1, updated.getTotalInvalid());

        BulkImportOnboardResponseDto onboarded =
                bulkUserImportService.onboardSession(validated.getImportSessionId());

        assertEquals(3, onboarded.getTotalUsers());
        assertEquals(2, onboarded.getSuccessful());
        assertEquals(1, onboarded.getFailed());
        assertEquals("email_already_exists", onboarded.getFailedUsers().get(0).getReason());
        assertEquals(BulkUserImportSession.STATUS_COMPLETED,
                store.get(validated.getImportSessionId()).getStatus());

        verify(endUserService, times(2)).addEndUser(any(EndUserRequestDTO.class));
        verify(notificationClient).sendBulkImportSummaryNotification(any());

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.onboardSession(validated.getImportSessionId()));
    }

    private BulkUserImportSession copySession(BulkUserImportSession source) {
        List<BulkUserImportSession.BulkImportSessionRow> rows = source.getRows() == null
                ? List.of()
                : source.getRows().stream()
                .map(r -> BulkUserImportSession.BulkImportSessionRow.builder()
                        .rowIndex(r.getRowIndex())
                        .firstName(r.getFirstName())
                        .lastName(r.getLastName())
                        .email(r.getEmail())
                        .phoneNumber(r.getPhoneNumber())
                        .phoneCode(r.getPhoneCode())
                        .countryCode(r.getCountryCode())
                        .department(r.getDepartment())
                        .valid(r.isValid())
                        .failureReason(r.getFailureReason())
                        .build())
                .toList();
        return BulkUserImportSession.builder()
                .id(source.getId())
                .clientAdminId(source.getClientAdminId())
                .createdBy(source.getCreatedBy())
                .status(source.getStatus())
                .rows(new java.util.ArrayList<>(rows))
                .createdAt(source.getCreatedAt())
                .expiresAt(source.getExpiresAt())
                .build();
    }

    private MultipartFile csvFile(String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.csv");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        return file;
    }
}
