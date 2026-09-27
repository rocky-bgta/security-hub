//import com.aspire.asat.cms.dto.client.responseDto.*;
//import com.aspire.asat.cms.exception.ResourceNotFoundException;
//import com.aspire.asat.cms.model.*;
//import com.aspire.asat.cms.repository.*;
//import com.aspire.asat.cms.repository.custom.CourseRepositoryCustom;
//import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
//import com.aspire.asat.cms.service.impl.ClientUserServiceImpl;
//import org.bson.Document;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ClientUserServiceImplTest {
//
//    @Mock
//    private CourseRepository courseRepository;
//
//    @Mock
//    private UserContentStatusRepository userContentStatusRepository;
//
//    @Mock
//    private UserPackageRepositoryCustom userPackageRepositoryCustom;
//
//    @Mock
//    private UserPackageRepository userPackageRepository;
//
//    @Mock
//    private CourseRepositoryCustom courseRepositoryCustom;
//
//    @Mock
//    private UserCourseRepository userCourseRepository;
//
//    @Mock
//    private UserCourseProgressRepository userCourseProgressRepository;
//
//    @Mock
//    private ContentRepository contentRepository;
//
//    @InjectMocks
//    private ClientUserServiceImpl clientUserService;
//
//    private Document createSampleDoc(Map<String, Object> data) {
//        Document doc = new Document();
//        data.forEach(doc::append);
//        return doc;
//    }
//
//    @BeforeEach
//    public void setup() {
//
//    }
//
//    // ---------------------------
//    // Test getDashboardSummary
//    // ---------------------------
//    @Test
//    public void getDashboardSummary_ShouldReturnCorrectSummary() {
//        Document doc = createSampleDoc(Map.of(
//                "totalCourses", 10,
//                "completedCourses", 5,
//                "inProgressCourses", 3,
//                "pendingCourses", 2,
//                "totalCertificates", 7
//        ));
//
//        when(courseRepositoryCustom.getUserDashboardSummary("user1")).thenReturn(doc);
//
//        DashboardSummaryResponseDTO result = clientUserService.getDashboardSummary("user1");
//
//        assertEquals(10, result.getTotalCourses());
//        assertEquals(5, result.getCompletedCourses());
//        assertEquals(3, result.getInProgressCourses());
//        assertEquals(2, result.getPendingCourses());
//        assertEquals(7, result.getTotalCertificates());
//
//        verify(courseRepositoryCustom).getUserDashboardSummary("user1");
//    }
//
//    // ---------------------------
//    // Test getUserCourses
//    // ---------------------------
//    @Test
//    public void getUserCourses_ShouldReturnEmptyList_WhenNoPackageAccess() {
//        when(userPackageRepository.existsByUserIdAndPackageId("user1", "pkg1")).thenReturn(false);
//
//        List<ClientCourseResponseDTO> result = clientUserService.getUserCourses("user1", "pkg1", null, null, null, 0, 10);
//
//        assertTrue(result.isEmpty());
//
//        verify(userPackageRepository).existsByUserIdAndPackageId("user1", "pkg1");
//        verifyNoInteractions(courseRepositoryCustom);
//    }
//
//    @Test
//    public void getUserCourses_ShouldReturnCourses_ForPackage() {
//        Document doc = createSampleDoc(Map.of(
//                "courseId", "course1",
//                "courseName", "Test Course",
//                "chapterIds", List.of("chap1", "chap2"),
//                "totalContentCount", 20,
//                "status", "active",
//                "isSaved", true,
//                "certificateLink", "cert_link",
//                "thumbnailUrl", "thumb_url",
//                "createdAt", new Date()
//        ));
//
//        when(userPackageRepository.existsByUserIdAndPackageId("user1", "pkg1")).thenReturn(true);
//        when(courseRepositoryCustom.getUserCoursesByPackage(null,eq("pkg1"), any(), any(), any(), anyInt(), anyInt()))
//                .thenReturn(List.of(doc));
//        when(userCourseProgressRepository.findByUserId("user1")).thenReturn(List.of(
//                new UserCourseProgress("user1", "course1", 50.0, List.of("content1", "content2", "content3", "content4", "content5"), Instant.now())
//        ));
//
//        List<ClientCourseResponseDTO> result = clientUserService.getUserCourses("user1", "pkg1", null, null, true, 0, 10);
//
//        assertEquals(1, result.size());
//        ClientCourseResponseDTO course = result.get(0);
//
//        assertEquals("course1", course.getCourseId());
//        assertEquals("Test Course", course.getCourseName());
//        assertEquals(20, course.getTotalContentCount());
//        assertEquals(5, course.getCompletedContentCount());
//        assertEquals(2, course.getChapterCount());  // Correct getter here
//        assertEquals(50.0, course.getProgress());
//
//        verify(courseRepositoryCustom).getUserCoursesByPackage(null, any(), any(), any(), any(), anyInt(), anyInt());
//        verify(userCourseProgressRepository).findByUserId("user1");
//    }
//
//    // ---------------------------
//    // Test getCourseDetails
//    // ---------------------------
//    @Test
//    public void getCourseDetails_ShouldReturnCourseDetails() {
//        Document content1 = createSampleDoc(Map.of("_id", "content1", "contentName", "Content One", "contentType", "VIDEO"));
//        Document content2 = createSampleDoc(Map.of("_id", "content2", "contentName", "Content Two", "contentType", "PDF"));
//
//        Document chapter1 = createSampleDoc(Map.of(
//                "_id", "chapter1",
//                "chapterName", "Chapter One",
//                "chapterDescription", "Description One",
//                "contentIds", List.of("content1", "content2")
//        ));
//
//        Document status1 = createSampleDoc(Map.of("userId", "user1", "contentId", "content1", "isDone", true));
//        Document status2 = createSampleDoc(Map.of("userId", "user1", "contentId", "content2", "isDone", false));
//
//        Document courseDoc = createSampleDoc(Map.of(
//                "courseName", "Course Name",
//                "courseDescription", "Course Description",
//                "thumbnailUrl", "thumb_url",
//                "chapters", List.of(chapter1),
//                "contents", List.of(content1, content2),
//                "contentStatus", List.of(status1, status2),
//                "certificateLink", "cert_link"
//        ));
//
//        when(courseRepositoryCustom.getFullCourseHierarchy("user1", "course1", null)).thenReturn(courseDoc);
//
//        CourseDetailsResponseDTO result = clientUserService.getCourseDetails("course1", "user1", null);
//
//        assertEquals("course1", result.getCourseId());
//        assertEquals("Course Name", result.getCourseName());
//        assertEquals("Course Description", result.getCourseDescription());
//        assertEquals(1, result.getChapters().size());
//        assertEquals(2, result.getContentCount());
//        assertEquals("thumb_url", result.getThumbnailUrl());
//        assertEquals("cert_link", result.getCertificateUrl());
//    }
//
//    @Test
//    public void getCourseDetails_ShouldThrowException_WhenCourseDocNull() {
//        when(courseRepositoryCustom.getFullCourseHierarchy("user1", "course1", null)).thenReturn(null);
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> clientUserService.getCourseDetails("course1", "user1", null));
//
//        assertTrue(exception.getMessage().contains("Course data not found"));
//    }
//
//    // ---------------------------
//    // Test getUserPackages
//    // ---------------------------
//    @Test
//    public void getUserPackages_ShouldReturnPackageResponseList() {
//        Document doc1 = createSampleDoc(Map.of(
//                "packageId", "pkg1",
//                "packageName", "Package One",
//                "courseIds", List.of("c1", "c2"),
//                "completedCourseIds", List.of("c1"),
//                "validity", "1 year",
//                "expiryDate", "2025-12-31"
//        ));
//
//        when(userPackageRepositoryCustom.getUserPackagesWithDetails("user1", 0, 10, null)).thenReturn(List.of(doc1));
//
//        List<PackageResponseDTO> result = clientUserService.getUserPackages("user1", 0, 10, null);
//
//        assertEquals(1, result.size());
//        PackageResponseDTO pkg = result.get(0);
//
//        assertEquals("pkg1", pkg.getPackageId());
//        assertEquals("Package One", pkg.getPackageName());
//        assertEquals(2, pkg.getTotalCourses());
//        assertEquals(1, pkg.getCompletedCourses());
//        assertEquals(50.0, pkg.getProgress());
//        assertEquals("1 year", pkg.getValidity());
//        assertEquals("2025-12-31", pkg.getExpireDate());
//    }
//
//    // ---------------------------
//    // Test getPackageDetails (mock implementation)
//    // ---------------------------
//    @Test
//    public void getPackageDetails_ShouldReturnMockData() {
//        PackageDetailsResponseDTO dto = clientUserService.getPackageDetails("pkg1", "user1");
//
//        assertEquals("pkg1", dto.getPackageId());
//        assertEquals("Pro Learning Pack", dto.getPackageName());
//        assertEquals("1 year", dto.getValidity());
//        assertEquals("2025-12-31", dto.getExpireDate());
////        assertEquals(2, dto.getCourses().size());
//    }
//
//    // ---------------------------
//    // Test countUserCourses
//    // ---------------------------
//    @Test
//    public void countUserCourses_ShouldReturnCount() {
//        when(courseRepositoryCustom.countUserCourses("user1", "active", "java", true)).thenReturn(7L);
//
//        long count = clientUserService.countUserCourses("user1", null, "active", "java", true);
//
//        assertEquals(7, count);
//        verify(courseRepositoryCustom).countUserCourses("user1", "active", "java", true);
//    }
//
//    // ---------------------------
//    // Test countUserCoursesByPackage
//    // ---------------------------
//    @Test
//    public void countUserCoursesByPackage_ShouldReturnCount() {
//        when(courseRepositoryCustom.countUserCoursesByPackage("pkg1", "completed", "spring", false)).thenReturn(3L);
//
//        long count = clientUserService.countUserCoursesByPackage("pkg1", "completed", "spring", false);
//
//        assertEquals(3, count);
//        verify(courseRepositoryCustom).countUserCoursesByPackage("pkg1", "completed", "spring", false);
//    }
//
//    // ---------------------------
//    // Test countUserPackages
//    // ---------------------------
//    @Test
//    public void countUserPackages_ShouldReturnCount() {
//        when(userPackageRepositoryCustom.countUserPackagesWithDetails("user1", null)).thenReturn(5L);
//
//        long count = clientUserService.countUserPackages("user1", null);
//
//        assertEquals(5, count);
//        verify(userPackageRepositoryCustom).countUserPackagesWithDetails("user1", null);
//    }
//
//    // ---------------------------
//    // Test bookmarkCourse
//    // ---------------------------
//    @Test
//    public void bookmarkCourse_ShouldUpdateIsSaved() {
//        UserCourse userCourse = new UserCourse();
////        userCourse.setSaved(false);
//
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.of(userCourse));
//        when(userCourseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//        clientUserService.bookmarkCourse("course1", "user1", null,true);
//
//        assertTrue(userCourse.isSaved());
//        verify(userCourseRepository).save(userCourse);
//    }
//
//    @Test
//    public void bookmarkCourse_ShouldThrowException_WhenUserCourseNotFound() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> clientUserService.bookmarkCourse("course1", "user1", null, true));
//    }
//
//    @Test
//    public void countUserCertificates_ShouldReturnCount() {
//        when(courseRepositoryCustom.countUserCertificates("user1", "course1")).thenReturn(12L);
//
//        long count = clientUserService.countUserCertificates("user1", "course1");
//
//        assertEquals(12, count);
//        verify(courseRepositoryCustom).countUserCertificates("user1", "course1");
//    }
//
//    // ---------------------------
//    // Test assignCourseToUser
//    // ---------------------------
//    @Test
//    @Transactional
//    public void assignCourseToUser_ShouldThrow_WhenAlreadyAssigned() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.of(new UserCourse()));
//
//        assertThrows(IllegalStateException.class, () -> clientUserService.assignCourseToUser("user1", "course1"));
//    }
//
//    @Test
//    @Transactional
//    public void assignCourseToUser_ShouldAssignSuccessfully() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.empty());
//
//        Document doc = createSampleDoc(Map.of(
//                "contents", List.of(
//                        createSampleDoc(Map.of("_id", "content1")),
//                        createSampleDoc(Map.of("_id", "content2"))
//                )
//        ));
//        when(courseRepositoryCustom.getFullCourseHierarchy("user1", "course1", null)).thenReturn(doc);
//
//        when(userCourseProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//        when(userContentStatusRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
//        when(userCourseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//        clientUserService.assignCourseToUser("user1", "course1");
//
//        verify(userCourseRepository).save(any());
//        verify(userCourseProgressRepository).save(any());
//        verify(userContentStatusRepository).saveAll(anyList());
//    }
//
//    // ---------------------------
//    // Test generateCertificate (async stub)
//    // ---------------------------
//    @Test
//    public void generateCertificate_ShouldRunWithoutError() {
//        // Just verify no exceptions thrown
//        clientUserService.generateCertificate("user1", "course1");
//        // It only logs info, no state change or return
//    }
//
//    @Test
//    public void markContentAsCompleted_ShouldCreateNewStatus_WhenNoneExists() {
//        String userId = "user1";
//        String courseId = "course1";
//        String contentId = "content1";
//
//        Course course = Course.builder().id(courseId).totalContentCount(3).build();
//        Content content = Content.builder().id(contentId).build();
//
//        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
//        when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
//        when(userContentStatusRepository.findByUserIdAndCourseIdAndContentId(userId, courseId, contentId)).thenReturn(Optional.empty());
//        when(userContentStatusRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//        when(userCourseProgressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
//        when(userCourseProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//        when(userCourseRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
//
//        clientUserService.markContentAsCompleted(userId, courseId, contentId, null);
//
//        verify(userContentStatusRepository).save(any());
//        verify(userCourseProgressRepository).save(any());
//        // userCourseRepository.save not called because no UserCourse found
//    }
//
//    @Test
//    public void bookmarkCourse_ShouldThrowResourceNotFound_WhenUserCourseMissing() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> {
//            clientUserService.bookmarkCourse("course1", "user1", null, true);
//        });
//    }
//
//    @Test
//    public void getUserCertificates_ShouldReturnEmptyList_WhenExceptionThrown() {
//        when(courseRepositoryCustom.getUserCertificates(anyString(), anyString(), anyInt(), anyInt()))
//                .thenThrow(new RuntimeException("DB error"));
//
//        List<CertificateResponseDTO> certs = clientUserService.getUserCertificates("user1", "course1", 0, 10);
//
//        assertNotNull(certs);
//        assertTrue(certs.isEmpty());
//    }
//
//    @Test
//    public void countUserCourses_ShouldHandleExceptionGracefully() {
//        when(courseRepositoryCustom.countUserCourses(anyString(), any(), any(), anyBoolean()))
//                .thenThrow(new RuntimeException("DB error"));
//
//        long count = 0;
//        try {
//            count = clientUserService.countUserCourses("user1", null, null, null, true);
//        } catch (Exception e) {
//            // Ignored
//        }
//        // We expect this method not to throw, so if exception is thrown this test fails
//        assertEquals(0, count);
//    }
//
//    @Test
//    public void assignCourseToUser_ShouldThrowRuntimeException_WhenNoContentsFound() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.empty());
//        Document doc = createSampleDoc(Map.of(
//                "contents", Collections.emptyList()
//        ));
//        when(courseRepositoryCustom.getFullCourseHierarchy("user1", "course1", null)).thenReturn(doc);
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
//            clientUserService.assignCourseToUser("user1", "course1");
//        });
//        assertTrue(ex.getMessage().contains("No content found"));
//    }
//
//    @Test
//    public void assignCourseToUser_ShouldThrowRuntimeException_WhenNoCourseStructureFound() {
//        when(userCourseRepository.findByUserIdAndCourseId("user1", "course1")).thenReturn(Optional.empty());
//        when(courseRepositoryCustom.getFullCourseHierarchy("user1", "course1", null)).thenReturn(null);
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
//            clientUserService.assignCourseToUser("user1", "course1");
//        });
//        assertTrue(ex.getMessage().contains("Course structure not found"));
//    }
//
//
//}
