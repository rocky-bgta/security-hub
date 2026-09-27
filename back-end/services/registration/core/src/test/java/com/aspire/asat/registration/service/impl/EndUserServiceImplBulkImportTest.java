package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.aspire.asat.registration.service.support.BulkImportFileParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplBulkImportTest {

    private static final String CLIENT_ADMIN_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String CSV_HEADER = "firstName,lastName,email,phoneNumber,department,countryCode\n";
    private static final String CSV_ROW = "John,Doe,john.doe@company.com,1234567890,IT,US\n";

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EndUserPackageRepository endUserPackageRepository;
    @Mock private AspireUserService aspireUserService;
    @Mock private DepartmentService departmentService;
    @Mock private RoleRepository roleRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private WebClient webClient;
    @Mock private UserLicenceRepository userLicenceRepository;
    @Mock private UserCurrentContextService currentContextService;
    @Mock private RegistrationNotificationClient notificationClient;
    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private UserSuspendReasonRepository userSuspendReasonRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private EndUserRepositoryCustom endUserRepositoryCustom;
    @Mock private com.aspire.asat.registration.repository.dropdown.TimezoneRepository timezoneRepository;
    @Mock private SimulationProductResolver simulationProductResolver;
    @Spy private BulkImportFileParser bulkImportFileParser = new BulkImportFileParser();

    @InjectMocks
    private EndUserServiceImpl endUserService;

    @Test
    void importEndUsersFromCsv_EmptyFile_ThrowsValidationException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        RegistationValidationException exception = assertThrows(
                RegistationValidationException.class,
                () -> endUserService.importEndUsersFromCsv(file, CLIENT_ADMIN_ID)
        );

        assertEquals("Import file is empty", exception.getMessage());
        verify(aspireUserService, never()).getUserByEmail(any());
    }

    @Test
    void importEndUsersFromCsv_UnsupportedExtension_ThrowsValidationException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("users.pdf");

        RegistationValidationException exception = assertThrows(
                RegistationValidationException.class,
                () -> endUserService.importEndUsersFromCsv(file, CLIENT_ADMIN_ID)
        );

        assertEquals("Only CSV and Excel files (.csv, .xls, .xlsx) are supported", exception.getMessage());
        verify(aspireUserService, never()).getUserByEmail(any());
    }

    @Test
    void importEndUsersFromCsv_ValidCsvExtension_ParsesRowsWithoutValidationFailure() throws IOException {
        MultipartFile file = csvFile("users.csv");
        stubNotificationContext();
        stubClientAdminDomainValidation();
        when(aspireUserService.getUserByEmail("john.doe@company.com"))
                .thenReturn(Optional.of(AspireUserDto.builder().email("john.doe@company.com").build()));

        List<EndUserResponseDTO> skippedUsers = endUserService.importEndUsersFromCsv(file, CLIENT_ADMIN_ID);

        assertEquals(1, skippedUsers.size());
        assertEquals("john.doe@company.com", skippedUsers.get(0).getEmail());
        assertEquals("duplicate", skippedUsers.get(0).getErrorReason());
        verify(notificationClient).sendBulkImportSummaryNotification(any());
    }

    @Test
    void importEndUsersFromCsv_UppercaseCsvExtension_ParsesRowsWithoutValidationFailure() throws IOException {
        MultipartFile file = csvFile("USERS.CSV");
        stubNotificationContext();
        stubClientAdminDomainValidation();
        when(aspireUserService.getUserByEmail("john.doe@company.com"))
                .thenReturn(Optional.of(AspireUserDto.builder().email("john.doe@company.com").build()));

        List<EndUserResponseDTO> skippedUsers = endUserService.importEndUsersFromCsv(file, CLIENT_ADMIN_ID);

        assertEquals(1, skippedUsers.size());
        assertEquals("duplicate", skippedUsers.get(0).getErrorReason());
    }

    private MultipartFile csvFile(String filename) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        byte[] content = (CSV_HEADER + CSV_ROW).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        return file;
    }

    private void stubNotificationContext() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .email("admin@company.com")
                .fullName("Admin User")
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        when(currentContextService.getCurrentUserContext()).thenReturn(context);
        when(aspireUserService.getUserById(UUID.fromString(CLIENT_ADMIN_ID)))
                .thenReturn(Optional.of(AspireUserDto.builder().companyName("Company").build()));
    }

    private void stubClientAdminDomainValidation() {
        when(aspireUserService.getUserById(UUID.fromString(CLIENT_ADMIN_ID)))
                .thenReturn(Optional.of(AspireUserDto.builder()
                        .email("admin@company.com")
                        .companyName("Company")
                        .build()));
    }
}
