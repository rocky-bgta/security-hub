import com.aspire.asat.registration.data.LicenseDto;
import com.aspire.asat.registration.model.License;
import com.aspire.asat.registration.repository.LicenseRepository;
import com.aspire.asat.registration.service.impl.LicenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LicenseServiceImplTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LicenseServiceImpl licenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldSaveAndReturnLicenseDto() {

        LicenseDto licenseDto = new LicenseDto();
        License license = new License();
        when(licenseRepository.save(any(License.class))).thenReturn(license);

        LicenseDto result = licenseService.save(licenseDto);

        assertNotNull(result);
        verify(licenseRepository, times(1)).save(any(License.class));
    }

    @Test
    void getAllLicense_ShouldReturnPaginatedLicenseList() {

        License license = new License();
        List<License> licenseList = Arrays.asList(license);
        Page<License> licensePage = new PageImpl<>(licenseList);
        when(licenseRepository.findAll(any(Pageable.class))).thenReturn(licensePage);

        List<LicenseDto> result = licenseService.getAllLicense(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(licenseRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void licenseCount_ShouldReturnTotalLicenseCount() {

        when(licenseRepository.count()).thenReturn(100L);

        long result = licenseService.licenseCount();

        assertEquals(100L, result);
        verify(licenseRepository, times(1)).count();
    }

    @Test
    void countValidLicenses_ShouldReturnCorrectCount() {

        License validLicense = new License();
        AggregationResults<License> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(validLicense));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("license"), eq(License.class)))
                .thenReturn(aggregationResults);

        long result = licenseService.countValidLicenses();

        assertEquals(1, result);
        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("license"), eq(License.class));
    }

    @Test
    void countExpiredLicenses_ShouldReturnCorrectCount() {

        License expiredLicense = new License();
        AggregationResults<License> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(expiredLicense));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("license"), eq(License.class)))
                .thenReturn(aggregationResults);

        long result = licenseService.countExpiredLicenses();

        assertEquals(1, result);
        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("license"), eq(License.class));
    }

    @Test
    void countLicensesExpiring_ShouldReturnCorrectCount() {

        License expiringLicense = new License();
        AggregationResults<License> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(expiringLicense));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("license"), eq(License.class)))
                .thenReturn(aggregationResults);

        long result = licenseService.countLicensesExpiring();

        assertEquals(1, result);
        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("license"), eq(License.class));
    }

}
