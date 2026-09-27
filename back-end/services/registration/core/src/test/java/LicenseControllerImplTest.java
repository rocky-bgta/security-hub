import com.aspire.asat.registration.controller.impl.LicenseControllerImpl;
import com.aspire.asat.registration.data.LicenseDto;
import com.aspire.asat.registration.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LicenseControllerImplTest {

    @Mock
    private LicenseService licenseService;

    @InjectMocks
    private LicenseControllerImpl licenseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createLicense_ShouldReturnCreatedLicense() {

        LicenseDto licenseDto = new LicenseDto();
        when(licenseService.save(any(LicenseDto.class))).thenReturn(licenseDto);

        ResponseEntity<LicenseDto> response = licenseController.createLicense(licenseDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(licenseDto, response.getBody());
        verify(licenseService, times(1)).save(any(LicenseDto.class));
    }

    @Test
    void getAllLicense_ShouldReturnListOfLicenses() {

        List<LicenseDto> licenses = Arrays.asList(new LicenseDto(), new LicenseDto());
        when(licenseService.getAllLicense(anyInt(), anyInt())).thenReturn(licenses);

        ResponseEntity<List<LicenseDto>> response = licenseController.getAllLicense(0, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(licenses, response.getBody());
        verify(licenseService, times(1)).getAllLicense(anyInt(), anyInt());
    }
}