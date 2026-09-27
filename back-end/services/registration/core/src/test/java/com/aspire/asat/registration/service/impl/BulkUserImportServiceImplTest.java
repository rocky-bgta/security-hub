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
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.BulkUserImportSession;
import com.aspire.asat.registration.repository.BulkUserImportSessionRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.EndUserService;
import com.aspire.asat.registration.service.support.BulkImportFileParser;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkUserImportServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SESSION_ID = "session-123";
    private static final String CSV_HEADER = "firstName,lastName,email,phoneNumber,department,countryCode\n";

    @Spy
    private BulkImportFileParser bulkImportFileParser = new BulkImportFileParser();
    @Mock
    private BulkUserImportSessionRepository sessionRepository;
    @Mock
    private AspireUserService aspireUserService;
    @Mock
    private EndUserService endUserService;
    @Mock
    private UserCurrentContextService currentContextService;
    @Mock
    private RegistrationNotificationClient notificationClient;

    @InjectMocks
    private BulkUserImportServiceImpl bulkUserImportService;

    @BeforeEach
    void setUpContext() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .email("admin@company.com")
                .fullName("Admin User")
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        lenient().when(currentContextService.getCurrentUserContext()).thenReturn(context);
    }

    @Test
    void validateImport_EmptyFile_ThrowsValidationException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.validateImport(file, CLIENT_ADMIN_ID, 0, 10));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void validateImport_ValidAndInvalidRows_ReturnsPaginatedLists() throws IOException {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(aspireUserService.getUserByEmail("exists@company.com"))
                .thenReturn(Optional.of(AspireUserDto.builder().email("exists@company.com").build()));

        String csv = CSV_HEADER
                + "John,Doe,john@company.com,111,IT,US\n"
                + "Bad,Email,not-an-email,222,IT,US\n"
                + "Dup,User,exists@company.com,333,IT,US\n"
                + "Other,Domain,user@other.com,444,IT,US\n"
                + "Jane,Doe,jane@company.com,555,IT,US\n"
                + "Jane2,Doe,jane@company.com,666,IT,US\n";

        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(invocation -> {
            BulkUserImportSession session = invocation.getArgument(0);
            session.setId(SESSION_ID);
            return session;
        });

        BulkImportValidateResponseDto response =
                bulkUserImportService.validateImport(csvFile(csv), CLIENT_ADMIN_ID, 0, 10);

        assertEquals(SESSION_ID, response.getImportSessionId());
        assertEquals(1, response.getTotalValid());
        assertEquals(5, response.getTotalInvalid());
        assertEquals(1, response.getValidUsers().getTotal());
        assertEquals(5, response.getInvalidUsers().getTotal());

        List<String> invalidReasons = response.getInvalidUsers().getItems().stream()
                .map(BulkImportUserDto::getFailureReason)
                .toList();
        assertTrue(invalidReasons.contains(BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT));
        assertTrue(invalidReasons.contains(BulkUserImportServiceImpl.REASON_EMAIL_ALREADY_EXISTS));
        assertTrue(invalidReasons.contains(BulkUserImportServiceImpl.REASON_INVALID_DOMAIN));
        assertTrue(invalidReasons.contains(BulkUserImportServiceImpl.REASON_DUPLICATE_EMAIL));
    }

    @Test
    void getSessionUsers_PaginatesValidUsers() {
        BulkUserImportSession session = sampleSession(
                validRow(1, "a@company.com"),
                validRow(2, "b@company.com"),
                invalidRow(3, "bad", BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT)
        );
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        AllResponseDto<List<BulkImportUserDto>> page =
                bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 1, 1);

        assertEquals(2L, page.getTotal());
        assertEquals(1, page.getItems().size());
        assertEquals("b@company.com", page.getItems().get(0).getEmail());
    }

    @Test
    void updateSessionUsers_FixingInvalidEmail_MovesToValid() {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());

        BulkUserImportSession session = sampleSession(
                invalidRow(1, "bad-email", BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT)
        );
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of(BulkImportUserDto.builder()
                        .rowIndex(1)
                        .email("fixed@company.com")
                        .firstName("John")
                        .lastName("Doe")
                        .phoneNumber("123")
                        .department("IT")
                        .build()))
                .build();

        BulkImportValidateResponseDto response =
                bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10);

        assertEquals(1, response.getTotalValid());
        assertEquals(0, response.getTotalInvalid());
        assertNull(response.getValidUsers().getItems().get(0).getFailureReason());
        assertEquals("fixed@company.com", response.getValidUsers().getItems().get(0).getEmail());
    }

    @Test
    void updateSessionUsers_StillInvalid_StaysInvalid() {
        stubClientAdmin();

        BulkUserImportSession session = sampleSession(
                invalidRow(1, "bad-email", BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT)
        );
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of(BulkImportUserDto.builder()
                        .rowIndex(1)
                        .email("still-bad")
                        .build()))
                .build();

        BulkImportValidateResponseDto response =
                bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10);

        assertEquals(0, response.getTotalValid());
        assertEquals(1, response.getTotalInvalid());
        assertEquals(BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT,
                response.getInvalidUsers().getItems().get(0).getFailureReason());
    }

    @Test
    void onboardSession_CreatesValidUsersAndSkipsInvalid() {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(endUserService.addEndUser(any(EndUserRequestDTO.class)))
                .thenReturn(EndUserResponseDTO.builder().email("ok@company.com").build());

        BulkUserImportSession session = sampleSession(
                validRow(1, "ok@company.com"),
                invalidRow(2, "bad", BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT)
        );
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkImportOnboardResponseDto response = bulkUserImportService.onboardSession(SESSION_ID);

        assertEquals(2, response.getTotalUsers());
        assertEquals(1, response.getSuccessful());
        assertEquals(1, response.getFailed());
        assertEquals(BulkUserImportSession.STATUS_COMPLETED, session.getStatus());
        verify(endUserService).addEndUser(any(EndUserRequestDTO.class));
        verify(notificationClient).sendBulkImportSummaryNotification(any());
    }

    @Test
    void onboardSession_CreationFailure_CountedAsFailed() {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(endUserService.addEndUser(any(EndUserRequestDTO.class)))
                .thenThrow(new RuntimeException("db error"));

        BulkUserImportSession session = sampleSession(validRow(1, "ok@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkImportOnboardResponseDto response = bulkUserImportService.onboardSession(SESSION_ID);

        assertEquals(0, response.getSuccessful());
        assertEquals(1, response.getFailed());
        assertEquals(BulkUserImportServiceImpl.REASON_CREATION_FAILED, response.getFailedUsers().get(0).getReason());
        verify(notificationClient).sendBulkImportSummaryNotification(any());
    }

    @Test
    void onboardSession_CompletedSession_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "ok@company.com"));
        session.setStatus(BulkUserImportSession.STATUS_COMPLETED);
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.onboardSession(SESSION_ID));
        verify(endUserService, never()).addEndUser(any());
    }

    @Test
    void getSessionUsers_ExpiredSession_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "ok@company.com"));
        session.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10));

        ArgumentCaptor<BulkUserImportSession> captor = ArgumentCaptor.forClass(BulkUserImportSession.class);
        verify(sessionRepository).save(captor.capture());
        assertEquals(BulkUserImportSession.STATUS_EXPIRED, captor.getValue().getStatus());
    }

    @Test
    void getSessionUsers_MissingSession_ThrowsNotFound() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10));
    }

    @Test
    void validateImport_MissingRequiredField_MarkedInvalid() throws IOException {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(invocation -> {
            BulkUserImportSession session = invocation.getArgument(0);
            session.setId(SESSION_ID);
            return session;
        });

        String csv = CSV_HEADER + ",Doe,john@company.com,111,IT,US\n";
        BulkImportValidateResponseDto response =
                bulkUserImportService.validateImport(csvFile(csv), CLIENT_ADMIN_ID, 0, 10);

        assertEquals(0, response.getTotalValid());
        assertEquals(1, response.getTotalInvalid());
        assertEquals(BulkUserImportServiceImpl.REASON_MISSING_REQUIRED_FIELD,
                response.getInvalidUsers().getItems().get(0).getFailureReason());
        assertFalse(response.getInvalidUsers().getItems().isEmpty());
        assertNotNull(response.getImportSessionId());
    }

    @Test
    void validateImport_BlankClientAdminId_Throws() {
        MultipartFile file = mock(MultipartFile.class);
        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.validateImport(file, "  ", 0, 10));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void validateImport_UnsupportedExtension_Throws() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.txt");

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.validateImport(file, CLIENT_ADMIN_ID, 0, 10));
    }

    @Test
    void validateImport_PaginationClampedAndApplied() throws IOException {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(invocation -> {
            BulkUserImportSession session = invocation.getArgument(0);
            session.setId(SESSION_ID);
            return session;
        });

        StringBuilder csv = new StringBuilder(CSV_HEADER);
        for (int i = 1; i <= 5; i++) {
            csv.append("User").append(i).append(",Doe,user").append(i)
                    .append("@company.com,111,IT,US\n");
        }

        BulkImportValidateResponseDto response =
                bulkUserImportService.validateImport(csvFile(csv.toString()), CLIENT_ADMIN_ID, 2, 0);

        assertEquals(5, response.getTotalValid());
        assertEquals(2, response.getValidUsers().getOffset());
        assertEquals(10, response.getValidUsers().getPageSize()); // default when pageSize <= 0
        assertEquals(3, response.getValidUsers().getItems().size());
        assertEquals("user3@company.com", response.getValidUsers().getItems().get(0).getEmail());
    }

    @Test
    void getSessionUsers_PaginatesInvalidUsers() {
        BulkUserImportSession session = sampleSession(
                validRow(1, "a@company.com"),
                invalidRow(2, "bad1", BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT),
                invalidRow(3, "bad2", BulkUserImportServiceImpl.REASON_INVALID_DOMAIN)
        );
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        AllResponseDto<List<BulkImportUserDto>> page =
                bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.INVALID, 0, 1);

        assertEquals(2L, page.getTotal());
        assertEquals(1, page.getItems().size());
        assertEquals("bad1", page.getItems().get(0).getEmail());
        assertEquals(BulkUserImportServiceImpl.REASON_INVALID_EMAIL_FORMAT,
                page.getItems().get(0).getFailureReason());
    }

    @Test
    void getSessionUsers_UnauthorizedUser_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "a@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(currentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .userId("other-user-id")
                .clientAdminId(CLIENT_ADMIN_ID)
                .email("other@company.com")
                .build());

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10));
    }

    @Test
    void updateSessionUsers_EmptyUsers_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "a@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of())
                .build();

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10));
    }

    @Test
    void updateSessionUsers_UnknownRowIndex_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "a@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of(BulkImportUserDto.builder().rowIndex(99).email("x@company.com").build()))
                .build();

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10));
    }

    @Test
    void updateSessionUsers_MissingRowIndex_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "a@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of(BulkImportUserDto.builder().email("x@company.com").build()))
                .build();

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10));
    }

    @Test
    void onboardSession_NotificationFailure_DoesNotFailOnboard() {
        stubClientAdmin();
        when(aspireUserService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(endUserService.addEndUser(any(EndUserRequestDTO.class)))
                .thenReturn(EndUserResponseDTO.builder().email("ok@company.com").build());
        org.mockito.Mockito.doThrow(new RuntimeException("notify down"))
                .when(notificationClient).sendBulkImportSummaryNotification(any());

        BulkUserImportSession session = sampleSession(validRow(1, "ok@company.com"));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(BulkUserImportSession.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkImportOnboardResponseDto response = bulkUserImportService.onboardSession(SESSION_ID);

        assertEquals(1, response.getSuccessful());
        assertEquals(BulkUserImportSession.STATUS_COMPLETED, session.getStatus());
    }

    @Test
    void onboardSession_ExpiredStatus_Throws() {
        BulkUserImportSession session = sampleSession(validRow(1, "ok@company.com"));
        session.setStatus(BulkUserImportSession.STATUS_EXPIRED);
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.onboardSession(SESSION_ID));
        verify(endUserService, never()).addEndUser(any());
    }

    private void stubClientAdmin() {
        when(aspireUserService.getUserById(UUID.fromString(CLIENT_ADMIN_ID)))
                .thenReturn(Optional.of(AspireUserDto.builder()
                        .email("admin@company.com")
                        .companyName("Company")
                        .build()));
    }

    private MultipartFile csvFile(String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.csv");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        return file;
    }

    private BulkUserImportSession sampleSession(BulkUserImportSession.BulkImportSessionRow... rows) {
        return BulkUserImportSession.builder()
                .id(SESSION_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .createdBy(CLIENT_ADMIN_ID)
                .status(BulkUserImportSession.STATUS_VALIDATED)
                .rows(new ArrayList<>(List.of(rows)))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();
    }

    private BulkUserImportSession.BulkImportSessionRow validRow(int index, String email) {
        return BulkUserImportSession.BulkImportSessionRow.builder()
                .rowIndex(index)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .phoneNumber("1234567890")
                .department("IT")
                .countryCode("US")
                .valid(true)
                .build();
    }

    private BulkUserImportSession.BulkImportSessionRow invalidRow(int index, String email, String reason) {
        return BulkUserImportSession.BulkImportSessionRow.builder()
                .rowIndex(index)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .phoneNumber("1234567890")
                .department("IT")
                .countryCode("US")
                .valid(false)
                .failureReason(reason)
                .build();
    }
}
