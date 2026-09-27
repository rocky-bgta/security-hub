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
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end bulk onboard flow against a real MongoDB (Testcontainers).
 * External create/notification collaborators remain mocked.
 */
@Testcontainers(disabledWithoutDocker = true)
class BulkUserImportIntegrationTest {

    private static final String CLIENT_ADMIN_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String CSV_HEADER = "firstName,lastName,email,phoneNumber,department,countryCode\n";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private MongoTemplate mongoTemplate;
    private BulkUserImportSessionRepository sessionRepository;
    private AspireUserService aspireUserService;
    private EndUserService endUserService;
    private RegistrationNotificationClient notificationClient;
    private UserCurrentContextService currentContextService;
    private BulkUserImportServiceImpl bulkUserImportService;

    @BeforeEach
    void setUp() {
        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(
                        MongoClients.create(mongoDBContainer.getConnectionString()),
                        "bulk_user_import_it"));
        sessionRepository = new MongoRepositoryFactory(mongoTemplate)
                .getRepository(BulkUserImportSessionRepository.class);

        aspireUserService = mock(AspireUserService.class);
        endUserService = mock(EndUserService.class);
        notificationClient = mock(RegistrationNotificationClient.class);
        currentContextService = mock(UserCurrentContextService.class);

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

        AtomicInteger created = new AtomicInteger();
        when(endUserService.addEndUser(any(EndUserRequestDTO.class))).thenAnswer(inv -> {
            EndUserRequestDTO req = inv.getArgument(0);
            created.incrementAndGet();
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

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(BulkUserImportSession.class);
    }

    @Test
    void fullFlow_ValidateUpdateOnboard_PersistsSessionAndCreatesUsers() throws IOException {
        String csv = CSV_HEADER
                + "John,Doe,john@company.com,111,IT,US\n"
                + "Bad,Email,not-an-email,222,IT,US\n"
                + "Exists,User,exists@company.com,333,IT,US\n";

        BulkImportValidateResponseDto validated =
                bulkUserImportService.validateImport(csvFile(csv), CLIENT_ADMIN_ID, 0, 10);

        assertNotNull(validated.getImportSessionId());
        assertEquals(1, validated.getTotalValid());
        assertEquals(2, validated.getTotalInvalid());

        BulkUserImportSession stored = sessionRepository.findById(validated.getImportSessionId()).orElseThrow();
        assertEquals(BulkUserImportSession.STATUS_VALIDATED, stored.getStatus());
        assertEquals(3, stored.getRows().size());
        assertNotNull(stored.getExpiresAt());

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

        BulkUserImportSession completed = sessionRepository.findById(validated.getImportSessionId()).orElseThrow();
        assertEquals(BulkUserImportSession.STATUS_COMPLETED, completed.getStatus());

        verify(endUserService, times(2)).addEndUser(any(EndUserRequestDTO.class));
        verify(notificationClient).sendBulkImportSummaryNotification(any());

        assertThrows(RegistationValidationException.class,
                () -> bulkUserImportService.onboardSession(validated.getImportSessionId()));
    }

    @Test
    void validateImport_PersistsAllValidationReasons() throws IOException {
        String csv = CSV_HEADER
                + "Ok,User,ok@company.com,111,IT,US\n"
                + "Bad,Fmt,bad-email,222,IT,US\n"
                + "Wrong,Domain,user@other.com,333,IT,US\n"
                + "Dup1,User,dup@company.com,444,IT,US\n"
                + "Dup2,User,dup@company.com,555,IT,US\n"
                + ",Missing,missing@company.com,666,IT,US\n"
                + "Exists,User,exists@company.com,777,IT,US\n";

        BulkImportValidateResponseDto response =
                bulkUserImportService.validateImport(csvFile(csv), CLIENT_ADMIN_ID, 0, 20);

        assertEquals(1, response.getTotalValid());
        assertEquals(6, response.getTotalInvalid());

        List<String> reasons = response.getInvalidUsers().getItems().stream()
                .map(BulkImportUserDto::getFailureReason)
                .toList();
        assertTrue(reasons.contains("invalid_email_format"));
        assertTrue(reasons.contains("invalid_domain"));
        assertTrue(reasons.contains("duplicate_email"));
        assertTrue(reasons.contains("missing_required_field"));
        assertTrue(reasons.contains("email_already_exists"));

        assertTrue(sessionRepository.findById(response.getImportSessionId()).isPresent());
        assertFalse(response.getValidUsers().getItems().isEmpty());
        assertEquals("ok@company.com", response.getValidUsers().getItems().get(0).getEmail());
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
