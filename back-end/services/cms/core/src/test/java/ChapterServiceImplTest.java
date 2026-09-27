import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.dto.enums.ChapterStatus;
import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Chapter;
import com.aspire.asat.cms.model.Content;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.impl.ChapterServiceImpl;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChapterServiceImplTest {

    @Mock
    ChapterRepository chapterRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    TopicRepository topicRepository;

    @Mock
    FileService fileService;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    ChapterServiceImpl chapterService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void saveChapter_success() {
        ChapterRequestDto dto = ChapterRequestDto.builder()
                .chapterName("Chapter 1")
                .topicId("topic1")
                .chapterStatus(ChapterStatus.ENABLED)
                .position(1)
                .contentIds(List.of("content1"))
                .build();

        when(chapterRepository.existsByChapterName("Chapter 1")).thenReturn(false);

        Chapter savedChapter = Chapter.builder()
                .id("chapter1")
                .chapterName("Chapter 1")
                .topicId("topic1")
                .build();

        Topic topic = Topic.builder()
                .id("topic1")
                .chapterIds(new ArrayList<>())
                .build();

        when(chapterRepository.save(any())).thenReturn(savedChapter);
        when(topicRepository.findById("topic1")).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChapterResponseDto response = chapterService.saveChapter(dto);

        assertEquals("chapter1", response.getId());
        verify(topicRepository).save(any());
    }

    @Test
    void saveChapter_courseNotFound_throws() {
        ChapterRequestDto dto = ChapterRequestDto.builder()
                .chapterName("Chapter 1")
                .topicId("topic1")
                .chapterStatus(ChapterStatus.ENABLED)
                .position(1)
                .build();

        when(chapterRepository.existsByChapterName("Chapter 1")).thenReturn(false);
        when(topicRepository.findById("topic1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chapterService.saveChapter(dto));
    }

    @Test
    void getAllChapters_search() {
        Chapter chapter = Chapter.builder().id("1").chapterName("Alpha").build();
        Page<Chapter> page = new PageImpl<>(List.of(chapter));

        when(chapterRepository.findByChapterName(eq("Alpha"), any(Pageable.class))).thenReturn(page);

        List<ChapterResponseDto> result = chapterService.getAllChapters("Alpha", null, 0, 10, "createdAt", "asc");
        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).getChapterName());
    }

    @Test
    void getAllChapters_topicId() {
        Chapter chapter = Chapter.builder().id("2").topicId("topic1").build();
        Page<Chapter> page = new PageImpl<>(List.of(chapter));

        when(chapterRepository.findByTopicId(eq("topic1"), any(Pageable.class))).thenReturn(page);

        List<ChapterResponseDto> result = chapterService.getAllChapters(null, "topic1", 0, 10, "createdAt", "desc");
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getId());
    }

    @Test
    void getAllChapters_noSearchOrTopic() {
        Chapter chapter = Chapter.builder().id("3").build();
        Page<Chapter> page = new PageImpl<>(List.of(chapter));

        when(chapterRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ChapterResponseDto> result = chapterService.getAllChapters(null, null, 0, 10, "createdAt", "asc");
        assertEquals(1, result.size());
    }
    @Test
    void getChapterById_success() {
        String chapterId = "ch1";

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .contentIds(List.of("content1"))
                .build();

        Content content = Content.builder()
                .id("content1")
                .contentName("Content 1")
                .description("Desc")
                .contentType(ContentType.VIDEO)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of(chapterId))
                .tags(List.of("tag1"))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(contentRepository.findAllById(chapter.getContentIds())).thenReturn(List.of(content));

        ChapterResponseWithItemDto result = chapterService.getChapterById(chapterId);

        assertEquals(chapterId, result.getId());
        assertEquals(1, result.getContentIds().size());
        assertEquals("Content 1", result.getContentIds().get(0).getCommon().getContentName());
    }

    @Test
    void getChapterById_notFound_throws() {
        when(chapterRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chapterService.getChapterById("nonexistent"));
    }

    @Test
    void updateChapterById_success() {
        String chapterId = "ch1";

        Chapter existing = Chapter.builder()
                .id(chapterId)
                .chapterName("OldName")
                .createdAt(Instant.now())
                .build();

        ChapterUpdateDto dto = ChapterUpdateDto.builder()
                .chapterName("NewName")
                .chapterStatus(ChapterStatus.ENABLED)
                .topicId("topic1")
                .position(1)
                .contentType(ContentType.VIDEO)
                .contentIds(List.of("content1"))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(existing));
        when(chapterRepository.existsByChapterName("NewName")).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChapterResponseDto result = chapterService.updateChapterById(chapterId, dto);

        assertEquals("NewName", result.getChapterName());
    }

    @Test
    void updateChapterById_duplicateName_throws() {
        String chapterId = "ch1";

        Chapter existing = Chapter.builder().id(chapterId).chapterName("OldName").build();

        ChapterUpdateDto dto = ChapterUpdateDto.builder()
                .chapterName("ExistingName")
                .chapterStatus(ChapterStatus.ENABLED)
                .topicId("topic1")
                .position(1)
                .contentType(ContentType.VIDEO)
                .contentIds(List.of("content1"))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(existing));
        when(chapterRepository.existsByChapterName("ExistingName")).thenReturn(true);

        DuplicateNameException ex = assertThrows(DuplicateNameException.class,
                () -> chapterService.updateChapterById(chapterId, dto));
        assertTrue(ex.getMessage().contains("Chapter already exists"));
    }

    @Test
    void updateChapterById_notFound_throws() {
        when(chapterRepository.findById("nonexistent")).thenReturn(Optional.empty());
        ChapterUpdateDto dto = ChapterUpdateDto.builder()
                .chapterName("Name")
                .chapterStatus(ChapterStatus.ENABLED)
                .topicId("topic1")
                .position(1)
                .contentType(ContentType.VIDEO)
                .contentIds(List.of("content1"))
                .build();

        assertThrows(ResourceNotFoundException.class, () -> chapterService.updateChapterById("nonexistent", dto));
    }
    @Test
    void deleteChapterById_success() {
        String chapterId = "ch1";
        List<String> contentIds = List.of("c1", "c2");

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .topicId("topic1")
                .contentIds(contentIds)
                .build();

        Topic topic = Topic.builder()
                .id("topic1")
                .totalContentCount(5)
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(topicRepository.findById("topic1")).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(contentRepository).deleteById(anyString());
        doNothing().when(chapterRepository).delete(any());

        ChapterResponseDto response = chapterService.deleteChapterById(chapterId);

        verify(contentRepository, times(contentIds.size())).deleteById(anyString());
        verify(chapterRepository).delete(chapter);
        verify(topicRepository).save(any());

        assertEquals(chapterId, response.getId());
    }

    @Test
    void deleteChapterById_chapterNotFound_throws() {
        when(chapterRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chapterService.deleteChapterById("nonexistent"));
    }

    @Test
    void deleteChapterById_courseNotFound_throws() {
        Chapter chapter = Chapter.builder()
                .id("ch1")
                .topicId("topic1")
                .build();

        when(chapterRepository.findById("ch1")).thenReturn(Optional.of(chapter));
        when(topicRepository.findById("topic1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chapterService.deleteChapterById("ch1"));
    }
    @Test
    void exportChaptersToCsv_success() throws IOException {
        ChapterResponseDto dto = ChapterResponseDto.builder()
                .id("ch1")
                .topicId("topic1")
                .chapterName("Chapter 1")
                .chapterStatus(ChapterStatus.ENABLED)
                .position(1)
                .contentIds(List.of("content1"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ChapterServiceImpl spyService = spy(chapterService);
        doReturn(List.of(dto)).when(spyService).getAllChapters(null, null, 0, 10, "chapterName", "asc");

        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        spyService.exportChaptersToCsv(0, 10, response);

        pw.flush();
        String csv = sw.toString();
        assertTrue(csv.contains("Chapter 1"));
    }
    @Test
    void updateChapterContentIds_success() {
        String chapterId = "ch1";
        List<String> newContentIds = List.of("content2");

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .contentIds(new ArrayList<>(List.of("content1")))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChapterResponseDto result = chapterService.updateChapterContentIds(chapterId, newContentIds);

        assertEquals(2, result.getContentIds().size());
        assertTrue(result.getContentIds().containsAll(List.of("content1", "content2")));
    }

    @Test
    void updateChapterContentIds_notFound_throws() {
        when(chapterRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chapterService.updateChapterContentIds("nonexistent", List.of("content")));
    }
    @Test
    void getTotalChapterCount_success() {
        when(chapterRepository.countByTopicId("topic1")).thenReturn(42L);
        long count = chapterService.getTotalChapterCount("topic1");
        assertEquals(42L, count);
    }
    @Test
    void removeContentFromChapter_success() {
        String chapterId = "ch1";
        String contentId = "content1";

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .contentIds(new ArrayList<>(List.of("content1", "content2")))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChapterResponseDto result = chapterService.removeContentFromChapter(chapterId, contentId);

        assertEquals(1, result.getContentIds().size());
        assertFalse(result.getContentIds().contains(contentId));
    }

    @Test
    void removeContentFromChapter_contentIdNotPresent() {
        String chapterId = "ch1";
        String contentId = "contentX";

        Chapter chapter = Chapter.builder()
                .id(chapterId)
                .contentIds(new ArrayList<>(List.of("content1", "content2")))
                .build();

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));

        ChapterResponseDto result = chapterService.removeContentFromChapter(chapterId, contentId);

        assertEquals(2, result.getContentIds().size());
    }

    @Test
    void removeContentFromChapter_notFound_throws() {
        when(chapterRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chapterService.removeContentFromChapter("nonexistent", "content1"));
    }



}

