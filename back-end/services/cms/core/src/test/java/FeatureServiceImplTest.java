import com.aspire.asat.cms.dto.enums.Availability;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.NullException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.repository.FeatureRepository;
import com.aspire.asat.cms.repository.PackageRepository;
import com.aspire.asat.cms.service.impl.FeatureServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class FeatureServiceImplTest {

    @Mock
    private FeatureRepository featureRepository;
    @Mock
    private PackageRepository packageRepository;
    @InjectMocks
    private FeatureServiceImpl featureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void saveFeature_shouldSaveAndReturnFeature() {
        RequestDto dto = RequestDto.builder()
                .featureName("Test Feature")
                .featureStatus(FeatureStatus.ENABLED)
                .build();

        when(featureRepository.existsByFeatureName(anyString())).thenReturn(false);
        when(featureRepository.save(any(Feature.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseDto result = featureService.saveFeature(dto);

        assertNotNull(result.getId());
        assertEquals("Test Feature", result.getFeatureName());
        verify(featureRepository).save(any(Feature.class));
    }

    @Test
    void saveFeature_shouldThrowWhenFeatureNameNull() {
        RequestDto dto = RequestDto.builder().featureName("").featureStatus(FeatureStatus.ENABLED).build();
        assertThrows(NullException.class, () -> featureService.saveFeature(dto));
    }

    @Test
    void saveFeature_shouldThrowOnDuplicateName() {
        RequestDto dto = RequestDto.builder().featureName("DUP").featureStatus(FeatureStatus.ENABLED).build();
        when(featureRepository.existsByFeatureName("DUP")).thenReturn(true);
        assertThrows(DuplicateNameException.class, () -> featureService.saveFeature(dto));
    }

    @Test
    void getAllFeatures_shouldReturnFeaturesWithoutSearch() {
        Feature f = Feature.builder().id("1").featureName("A").featureStatus(FeatureStatus.ENABLED).build();
        Page<Feature> page = new PageImpl<>(List.of(f));
        when(featureRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ResponseDto> list = featureService.getAllFeatures(null, 0, 10, "createdAt", "asc");
        assertEquals(1, list.size());
        assertEquals("A", list.get(0).getFeatureName());
    }

    @Test
    void getAllFeatures_shouldReturnFeaturesWithSearch() {
        Feature f = Feature.builder().id("1").featureName("B").featureStatus(FeatureStatus.ENABLED).build();
        Page<Feature> page = new PageImpl<>(List.of(f));
        when(featureRepository.findByFeatureName(eq("B"), any(Pageable.class))).thenReturn(page);

        List<ResponseDto> list = featureService.getAllFeatures("B", 0, 10, "createdAt", "asc");
        assertEquals(1, list.size());
        assertEquals("B", list.get(0).getFeatureName());
    }

    @Test
    void getFeatureById_shouldReturnDetails() {
        Feature feature = Feature.builder()
                .id("1")
                .featureName("Test")
                .featureStatus(FeatureStatus.ENABLED)
                .packageIds(List.of("p1"))
                .build();

        // Step 1: Use entity, not DTO!
        com.aspire.asat.cms.model.Package pkgEntity = com.aspire.asat.cms.model.Package.builder()
                .id("p1")
                .packageName("Pkg")
                .build();

        // Step 2: Mock repository with the entity
        when(featureRepository.findById("1")).thenReturn(Optional.of(feature));
        when(packageRepository.findAllById(anyList())).thenReturn(List.of(pkgEntity));

        // Test
        var result = featureService.getFeatureById("1");
        assertEquals("Test", result.getFeatureName());
        assertEquals(1, result.getPackageIds().size());
    }



    @Test
    void getFeatureById_notFound_throws() {
        when(featureRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> featureService.getFeatureById("2"));
    }


    @Test
    void updateFeatureById_shouldUpdateAndReturn() {
        Feature existing = Feature.builder().id("1").featureName("Old").featureStatus(FeatureStatus.ENABLED)
                .createdAt(Instant.now()).build();
        UpdateRequestDto dto = UpdateRequestDto.builder().featureName("New")
                .featureStatus(FeatureStatus.ENABLED).createdAt(existing.getCreatedAt()).updatedAt(Instant.now()).build();

        when(featureRepository.findById("1")).thenReturn(Optional.of(existing));
        when(featureRepository.existsByFeatureName("New")).thenReturn(false);
        when(featureRepository.save(any(Feature.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = featureService.updateFeatureById("1", dto);
        assertEquals("New", result.getFeatureName());
    }

    @Test
    void updateFeatureById_duplicateName_throws() {
        Feature existing = Feature.builder().id("1").featureName("Old").featureStatus(FeatureStatus.ENABLED)
                .createdAt(Instant.now()).build();
        UpdateRequestDto dto = UpdateRequestDto.builder().featureName("Other").featureStatus(FeatureStatus.ENABLED)
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now()).build();

        when(featureRepository.findById("1")).thenReturn(Optional.of(existing));
        when(featureRepository.existsByFeatureName("Other")).thenReturn(true);

        assertThrows(DuplicateNameException.class, () -> featureService.updateFeatureById("1", dto));
    }

    @Test
    void updateFeatureById_notFound_throws() {
        UpdateRequestDto dto = UpdateRequestDto.builder().featureName("A").featureStatus(FeatureStatus.ENABLED)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(featureRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> featureService.updateFeatureById("1", dto));
    }

    @Test
    void updateFeatureById_nullName_throws() {
        Feature existing = Feature.builder().id("1").featureName("Old").featureStatus(FeatureStatus.ENABLED)
                .createdAt(Instant.now()).build();
        UpdateRequestDto dto = UpdateRequestDto.builder().featureName("").featureStatus(FeatureStatus.ENABLED)
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now()).build();

        when(featureRepository.findById("1")).thenReturn(Optional.of(existing));
        assertThrows(NullException.class, () -> featureService.updateFeatureById("1", dto));
    }

    @Test
    void deleteFeatureById_shouldDeleteAndReturnName() {
        Feature f = Feature.builder().id("1").featureName("TestDelete").build();
        when(featureRepository.findById("1")).thenReturn(Optional.of(f));

        String name = featureService.deleteFeatureById("1");
        assertEquals("TestDelete", name);
        verify(featureRepository).deleteById("1");
    }

    @Test
    void deleteFeatureById_notFound_throws() {
        when(featureRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> featureService.deleteFeatureById("2"));
    }
    @Test
    void exportFeatures_shouldWriteCsv() throws IOException {
        List<ResponseDto> features = List.of(
                ResponseDto.builder()
                        .id("1")
                        .featureName("Test")
                        .featureDescription("Desc")
                        .featureStatus(FeatureStatus.ENABLED)
                        .packageIds(List.of("p1"))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );

        FeatureServiceImpl spyService = Mockito.spy(featureService);
        doReturn(features).when(spyService).getAllFeatures(null, 0, 1000, "createdAt", "desc");

        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        spyService.exportFeatures(resp);

        pw.flush();
        String csv = sw.toString();
        assertTrue(csv.contains("Test"));
    }

    @Test
    void getTotalFeatureCount_shouldReturnCount() {
        when(featureRepository.count()).thenReturn(42L);
        assertEquals(42L, featureService.getTotalFeatureCount());
    }
    @Test
    void deleteFeaturesByIds_shouldDeleteAll() {
        Feature f1 = Feature.builder().id("1").featureName("A").build();
        Feature f2 = Feature.builder().id("2").featureName("B").build();
        when(featureRepository.findById("1")).thenReturn(Optional.of(f1));
        when(featureRepository.findById("2")).thenReturn(Optional.of(f2));

        List<String> names = featureService.deleteFeaturesByIds(List.of("1", "2"));
        assertEquals(List.of("A", "B"), names);
        verify(featureRepository, times(2)).deleteById(anyString());
    }
    @Test
    void updateFeaturesStatusByIds_shouldUpdateAll() {
        Feature f1 = Feature.builder().id("1").featureName("A").featureStatus(FeatureStatus.DISABLED).build();
        when(featureRepository.findById("1")).thenReturn(Optional.of(f1));
        when(featureRepository.save(any(Feature.class))).thenReturn(f1);

        List<String> names = featureService.updateFeaturesStatusByIds(List.of("1"), Status.ENABLED);
        assertEquals(List.of("A"), names);
        verify(featureRepository).save(any(Feature.class));
    }

    @Test
    void updateFeaturesStatusByIds_notFound_throws() {
        when(featureRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> featureService.updateFeaturesStatusByIds(List.of("x"), Status.ENABLED));
    }
    @Test
    void getAllFeaturesByStatus_shouldReturnEnabledOnly() {
        Feature f = Feature.builder().id("1").featureName("A").featureStatus(FeatureStatus.ENABLED).build();
        Page<Feature> page = new PageImpl<>(List.of(f));
        when(featureRepository.findByFeatureStatus(eq(FeatureStatus.ENABLED), any(Pageable.class))).thenReturn(page);

        List<ResponseDto> list = featureService.getAllFeaturesByStatus(null, 0, 10, "createdAt", "desc");
        assertEquals(1, list.size());
        assertEquals("A", list.get(0).getFeatureName());
    }
    @Test
    void checkUniqueFeatureName_duplicate_throws() {
        when(featureRepository.existsByFeatureName("X")).thenReturn(true);
        assertThrows(DuplicateNameException.class, () -> {
            // Using reflection to access private method for coverage
            try {
                var method = FeatureServiceImpl.class.getDeclaredMethod("checkUniqueFeatureName", String.class);
                method.setAccessible(true);
                method.invoke(featureService, "X");
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

}
