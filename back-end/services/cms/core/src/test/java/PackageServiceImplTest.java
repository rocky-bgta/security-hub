import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.packageDto.*;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.NullException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.*;
import com.aspire.asat.cms.model.Package;
import com.aspire.asat.cms.repository.*;
import com.aspire.asat.cms.service.impl.PackageServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;

class PackageServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private PackageServiceImpl packageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /* savePackage tests */

    @Test
    void savePackage_shouldSaveSuccessfully() {
        RequestDto requestDto = RequestDto.builder()
                .packageName("Test Package")
                .price(100.0)
                .packageStatus(PackageStatus.ENABLED)
                .build();

        when(packageRepository.existsByPackageName("Test Package")).thenReturn(false);

        // Mock save to return the Package converted back to ResponseDto
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseDto response = packageService.savePackage(requestDto);

        assertNotNull(response);
        assertEquals("Test Package", response.getPackageName());
        assertEquals(100.0, response.getPrice());
        assertEquals(PackageStatus.ENABLED, response.getPackageStatus());
        verify(packageRepository, times(1)).existsByPackageName("Test Package");
        verify(packageRepository, times(1)).save(any(Package.class));
    }

    @Test
    void savePackage_shouldThrowNullException_whenPackageNameNull() {
        RequestDto requestDto = RequestDto.builder()
                .packageName(null)
                .price(100.0)
                .packageStatus(PackageStatus.ENABLED)
                .build();

        NullException ex = assertThrows(NullException.class, () -> packageService.savePackage(requestDto));
        assertEquals("Package name cannot be null or empty", ex.getMessage());
        verify(packageRepository, never()).save(any());
    }

    @Test
    void savePackage_shouldThrowDuplicateNameException_whenPackageNameExists() {
        RequestDto requestDto = RequestDto.builder()
                .packageName("Duplicate Package")
                .price(100.0)
                .packageStatus(PackageStatus.ENABLED)
                .build();

        when(packageRepository.existsByPackageName("Duplicate Package")).thenReturn(true);

        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> packageService.savePackage(requestDto));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(packageRepository, never()).save(any());
    }

    /* getAllPackages tests */

    @Test
    void getAllPackages_shouldReturnAll_whenNoSearch() {
        Package pkg1 = Package.builder().id("1").packageName("Pkg1").price(10).packageStatus(PackageStatus.ENABLED).build();
        Package pkg2 = Package.builder().id("2").packageName("Pkg2").price(20).packageStatus(PackageStatus.DISABLED).build();

        Page<Package> page = new PageImpl<>(List.of(pkg1, pkg2));

        when(packageRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ResponseDto> results = packageService.getAllPackages(null, 0, 10, "packageName", "asc");

        assertEquals(2, results.size());
        assertEquals("Pkg1", results.get(0).getPackageName());
        verify(packageRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllPackages_shouldReturnFiltered_whenSearchProvided() {
        Package pkg = Package.builder().id("1").packageName("TestPackage").price(10).packageStatus(PackageStatus.ENABLED).build();
        Page<Package> page = new PageImpl<>(List.of(pkg));

        when(packageRepository.findByPackageName(eq("Test"), any(Pageable.class))).thenReturn(page);

        List<ResponseDto> results = packageService.getAllPackages("Test", 0, 10, "packageName", "desc");

        assertEquals(1, results.size());
        assertTrue(results.get(0).getPackageName().contains("Test"));
        verify(packageRepository).findByPackageName(eq("Test"), any(Pageable.class));
    }

    /* getPackageById tests */

//    @Test
//    void getPackageById_shouldReturnDtoWithDetails() {
//        String id = "package1";
//
//        Package pkg = Package.builder()
//                .id(id)
//                .packageName("Package 1")
//                .courseIds(List.of("course1", "course2"))
//                .featureIds(List.of("feature1"))
//                .packageStatus(PackageStatus.ENABLED)
//                .build();
//
//        Course courseEnabled = Course.builder()
//                .id("course1")
//                .courseName("Course 1")
//                .courseStatus(CourseStatus.ENABLED)
//                .build();
//
//        Course courseDisabled = Course.builder()
//                .id("course2")
//                .courseName("Course 2")
//                .courseStatus(CourseStatus.DISABLED)
//                .build();
//
//        Feature featureEnabled = Feature.builder()
//                .id("feature1")
//                .featureName("Feature 1")
//                .featureStatus(FeatureStatus.ENABLED)
//                .build();
//
//        Feature featureDisabled = Feature.builder()
//                .id("feature2")
//                .featureName("Feature 2")
//                .featureStatus(FeatureStatus.DISABLED)
//                .build();
//
//        when(packageRepository.findById(id)).thenReturn(Optional.of(pkg));
//
//        when(courseRepository.findAllById(pkg.getCourseIds())).thenReturn(List.of(courseEnabled, courseDisabled));
//        when(featureRepository.findAllById(pkg.getFeatureIds())).thenReturn(List.of(featureEnabled, featureDisabled));
//
//        ResponseDtoWithCourseFeatureDetails dto = packageService.getPackageById(id);
//
//        assertNotNull(dto);
//        assertEquals(id, dto.getId());
//        // Only ENABLED courses/features should be included
//        assertEquals(1, dto.getCourseIds().size());
//        assertEquals("Course 1", dto.getCourseIds().get(0).getCourseName());
//        assertEquals(1, dto.getFeatureIds().size());
//        assertEquals("Feature 1", dto.getFeatureIds().get(0).getFeatureName());
//
//        verify(packageRepository).findById(id);
//        verify(courseRepository).findAllById(pkg.getCourseIds());
//        verify(featureRepository).findAllById(pkg.getFeatureIds());
//    }

    @Test
    void getPackageById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(packageRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> packageService.getPackageById("invalid"));
    }

    /* updatePackageById tests */

    @Test
    void updatePackageById_shouldUpdateSuccessfully() {
        String id = "package1";
        UpdateRequestDto updateDto = UpdateRequestDto.builder()
                .packageName("Updated Name")
                .price(150)
                .packageStatus(PackageStatus.ENABLED)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();

        Package existingPackage = Package.builder()
                .id(id)
                .packageName("Old Name")
                .createdAt(Instant.now().minusSeconds(7200))
                .build();

        when(packageRepository.findById(id)).thenReturn(Optional.of(existingPackage));
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseDto response = packageService.updatePackageById(id, updateDto);

        assertEquals("Updated Name", response.getPackageName());
        assertEquals(150, response.getPrice());
        verify(packageRepository).save(any(Package.class));
    }

    @Test
    void updatePackageById_shouldThrowNullException_whenPackageNameNull() {
        String id = "package1";
        UpdateRequestDto updateDto = UpdateRequestDto.builder()
                .packageName(null)
                .price(150)
                .packageStatus(PackageStatus.ENABLED)
                .build();

        Package existingPackage = Package.builder().id(id).build();
        when(packageRepository.findById(id)).thenReturn(Optional.of(existingPackage));

        NullException ex = assertThrows(NullException.class, () -> packageService.updatePackageById(id, updateDto));
        assertEquals("Package name cannot be null or empty", ex.getMessage());
    }

    @Test
    void updatePackageById_shouldThrowResourceNotFoundException_whenPackageNotFound() {
        when(packageRepository.findById("invalid")).thenReturn(Optional.empty());
        UpdateRequestDto updateDto = UpdateRequestDto.builder()
                .packageName("Name")
                .price(100)
                .packageStatus(PackageStatus.ENABLED)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> packageService.updatePackageById("invalid", updateDto));
    }

    /* deletePackageById tests */

    @Test
    void deletePackageById_shouldDeleteSuccessfully() {
        String id = "package1";
        Package pkg = Package.builder()
                .id(id)
                .packageName("To Delete")
                .build();

        when(packageRepository.findById(id)).thenReturn(Optional.of(pkg));
        doNothing().when(packageRepository).deleteById(id);

        String deletedName = packageService.deletePackageById(id);
        assertEquals("To Delete", deletedName);
        verify(packageRepository).deleteById(id);
    }

    @Test
    void deletePackageById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(packageRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> packageService.deletePackageById("invalid"));
    }

    /* getTotalPackageCount tests */
//
//    @Test
//    void getTotalPackageCount_shouldReturnCount() {
//        when(packageRepository.count()).thenReturn(42L);
//        long count = packageService.getTotalPackageCount();
//        assertEquals(42, count);
//    }

    /* deletePackagesByIds tests */

    @Test
    void deletePackagesByIds_shouldDeleteAll() {
        Package pkg1 = Package.builder().id("1").packageName("P1").build();
        Package pkg2 = Package.builder().id("2").packageName("P2").build();

        when(packageRepository.findById("1")).thenReturn(Optional.of(pkg1));
        when(packageRepository.findById("2")).thenReturn(Optional.of(pkg2));
        doNothing().when(packageRepository).deleteById(anyString());

        List<String> deletedNames = packageService.deletePackagesByIds(List.of("1", "2"));
        assertEquals(2, deletedNames.size());
        assertTrue(deletedNames.contains("P1"));
        assertTrue(deletedNames.contains("P2"));
        verify(packageRepository, times(2)).deleteById(anyString());
    }

    /* updatePackagesStatusByIds tests */

    @Test
    void updatePackagesStatusByIds_shouldUpdateStatus() {
        Package pkg1 = Package.builder().id("1").packageName("P1").packageStatus(PackageStatus.ENABLED).build();
        Package pkg2 = Package.builder().id("2").packageName("P2").packageStatus(PackageStatus.DISABLED).build();

        when(packageRepository.findById("1")).thenReturn(Optional.of(pkg1));
        when(packageRepository.findById("2")).thenReturn(Optional.of(pkg2));
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> updatedNames = packageService.updatePackagesStatusByIds(List.of("1", "2"), Status.ENABLED);

        assertEquals(2, updatedNames.size());
        assertTrue(updatedNames.contains("P1"));
        assertTrue(updatedNames.contains("P2"));
        verify(packageRepository, times(2)).save(any(Package.class));
    }

    @Test
    void getAllPackages_shouldReturnEmptyList_whenNoPackages() {
        Page<Package> emptyPage = new PageImpl<>(Collections.emptyList());
        when(packageRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        List<ResponseDto> results = packageService.getAllPackages(null, 0, 10, "packageName", "asc");
        assertTrue(results.isEmpty());
    }

    @Test
    void getAllPackages_shouldHandleEmptySearchAsNoFilter() {
        Page<Package> page = new PageImpl<>(List.of());
        when(packageRepository.findAll(any(Pageable.class))).thenReturn(page);
        List<ResponseDto> results = packageService.getAllPackages("", 0, 10, "packageName", "asc");
        verify(packageRepository).findAll(any(Pageable.class));
        assertNotNull(results);
    }

    @Test
    void getAllPackages_shouldUseAscAndDescOrderCorrectly() {
        Page<Package> page = new PageImpl<>(List.of());
        when(packageRepository.findAll(any(Pageable.class))).thenReturn(page);

        packageService.getAllPackages(null, 0, 10, "packageName", "asc");
        packageService.getAllPackages(null, 0, 10, "packageName", "desc");

        verify(packageRepository, times(2)).findAll(any(Pageable.class));
    }

    @Test
    void getAllPackages_shouldDefaultToAsc_whenOrderIsInvalid() {
        Page<Package> page = new PageImpl<>(List.of());
        when(packageRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ResponseDto> results = packageService.getAllPackages(null, 0, 10, "packageName", "invalid");
        assertNotNull(results);
    }

    // exportPackages - success and failure

    @Test
    void exportPackages_shouldWriteCsvSuccessfully() throws IOException {
        ResponseDto dto = ResponseDto.builder()
                .id("1")
                .packageName("P1")
                .packageDescription("desc")
                .price(10)
                .packageStatus(PackageStatus.ENABLED)
                .courseIds(List.of("c1"))
                .featureIds(List.of("f1"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PackageServiceImpl spyService = Mockito.spy(packageService);
        doReturn(List.of(dto)).when(spyService).getAllPackages(null, 0, 1000, "createdAt", "desc");

        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        spyService.exportPackages(response);

        String csv = stringWriter.toString();
        assertTrue(csv.contains("Package Name"));
        assertTrue(csv.contains("P1"));
    }

    @Test
    void exportPackages_shouldThrowRuntimeException_onIOException() throws IOException {
        PackageServiceImpl spyService = Mockito.spy(packageService);
        doReturn(Collections.emptyList()).when(spyService).getAllPackages(null, 0, 1000, "createdAt", "desc");

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException("IO error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> spyService.exportPackages(response));
        assertTrue(ex.getMessage().contains("Failed to export"));
    }

    // exportBulkPackages - success and failure

    @Test
    void exportBulkPackages_shouldWriteCsvSuccessfully() throws IOException {
        String id = "1";
        Package pkg = Package.builder()
                .id(id)
                .packageName("P1")
                .packageDescription("desc")
                .price(10)
                .packageStatus(PackageStatus.ENABLED)
                .courseIds(List.of("c1"))
                .featureIds(List.of("f1"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(packageRepository.findById(id)).thenReturn(Optional.of(pkg));

        PackageServiceImpl spyService = Mockito.spy(packageService);
        List<String> ids = List.of(id);

        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        spyService.exportBulkPackages(ids, response);

        String csv = stringWriter.toString();
        assertTrue(csv.contains("Package Name"));
        assertTrue(csv.contains("P1"));
    }

    @Test
    void exportBulkPackages_shouldThrowRuntimeException_onIOException() throws IOException {
        List<String> ids = List.of("1");

        when(packageRepository.findById(anyString())).thenReturn(Optional.of(Package.builder().id("1").build()));

        PackageServiceImpl spyService = Mockito.spy(packageService);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException("IO error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> spyService.exportBulkPackages(ids, response));
        assertTrue(ex.getMessage().contains("Failed to export bulk"));
    }

    // deletePackagesByIds throws ResourceNotFoundException if any id missing

    @Test
    void deletePackagesByIds_shouldThrow_whenIdNotFound() {
        when(packageRepository.findById("1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> packageService.deletePackagesByIds(List.of("1")));
        assertTrue(ex.getMessage().contains("Resource not found"));
    }

    // updatePackagesStatusByIds throws ResourceNotFoundException if any id missing

    @Test
    void updatePackagesStatusByIds_shouldThrow_whenIdNotFound() {
        when(packageRepository.findById("1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> packageService.updatePackagesStatusByIds(List.of("1"), Status.ENABLED));
        assertTrue(ex.getMessage().contains("Resource not found"));
    }

    // Test empty courseIds or featureIds list for getPackageById
//
//    @Test
//    void getPackageById_shouldHandleEmptyCourseAndFeatureLists() {
//        String id = "package1";
//
//        Package pkg = Package.builder()
//                .id(id)
//                .packageName("Package 1")
//                .courseIds(Collections.emptyList())
//                .featureIds(Collections.emptyList())
//                .packageStatus(PackageStatus.ENABLED)
//                .build();
//
//        when(packageRepository.findById(id)).thenReturn(Optional.of(pkg));
//        when(courseRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
//        when(featureRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
//
//        ResponseDtoWithCourseFeatureDetails dto = packageService.getPackageById(id);
//        assertNotNull(dto);
//        assertTrue(dto.getCourseIds().isEmpty());
//        assertTrue(dto.getFeatureIds().isEmpty());
//    }

}
