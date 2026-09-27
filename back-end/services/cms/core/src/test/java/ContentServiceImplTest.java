import com.aspire.asat.cms.client.service.VideoContentProcessor;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Content;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.ChapterService;
import com.aspire.asat.cms.service.impl.ContentServiceImpl;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ChapterService chapterService;

    @Mock
    private VideoContentProcessor videoContentProcessor;

    @Mock
    private FileService fileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ContentServiceImpl contentService;

    @BeforeEach
    void initContentService() {
        MockitoAnnotations.openMocks(this);
        contentService = new ContentServiceImpl(
                contentRepository, topicRepository, chapterService,
                videoContentProcessor, fileService, objectMapper);
    }

    @Test
    void getAllContents_withSearch_returnsContents() {
        Content content = Content.builder()
                .id("c1")
                .contentName("Test Content")
                .build();

        Page<Content> page = new PageImpl<>(List.of(content));
        when(contentRepository.findByContentNameContainingIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(page);

        List<ContentRespDto<?>> result = contentService.getAllContents("Test", null, 0, 10, "createdAt", "asc");
        assertEquals(1, result.size());
        assertEquals("Test Content", result.get(0).getCommon().getContentName());
    }

    @Test
    void getAllContents_noSearch_returnsAll() {
        Content content = Content.builder()
                .id("c1")
                .contentName("All Content")
                .build();

        Page<Content> page = new PageImpl<>(List.of(content));
        when(contentRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ContentRespDto<?>> result = contentService.getAllContents(null, null, 0, 10, "createdAt", "asc");
        assertEquals(1, result.size());
        assertEquals("All Content", result.get(0).getCommon().getContentName());
    }
    @Test
    void getContentById_found_returnsDto() {
        Content content = Content.builder()
                .id("c1")
                .contentName("Sample")
                .build();

        when(contentRepository.findById("c1")).thenReturn(Optional.of(content));

        ContentRespDto<?> result = contentService.getContentById("c1");

        assertEquals("c1", result.getId());
        assertEquals("Sample", result.getCommon().getContentName());
    }

    @Test
    void getContentById_notFound_throws() {
        when(contentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contentService.getContentById("missing"));
    }
    @Test
    void getTotalContentCount_returnsCount() {
        when(contentRepository.count()).thenReturn(42L);

        long count = contentService.getTotalContentCount();
        assertEquals(42L, count);
    }
    @Test
    void deleteContentById_success_updatesCoursesAndDeletesContent() {
        Content content = Content.builder()
                .id("c1")
                .chapterIds(List.of("ch1"))
                .build();

        ChapterResponseWithItemDto chapter = ChapterResponseWithItemDto.builder()
                .id("ch1")
                .topicId("topic1")
                .build();

        Topic topic = Topic.builder()
                .id("topic1")
                .totalContentCount(3)
                .build();

        when(contentRepository.findById("c1")).thenReturn(Optional.of(content));
        when(chapterService.getChapterById("ch1")).thenReturn(chapter);
        when(topicRepository.findById("topic1")).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(contentRepository).delete(any());

        ContentRespDto<?> result = contentService.deleteContentById("c1");

        verify(topicRepository).save(any());
        verify(contentRepository).delete(content);

        assertEquals("c1", result.getId());
    }

    @Test
    void deleteContentById_contentNotFound_throws() {
        when(contentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contentService.deleteContentById("missing"));
    }
    @Test
    void createContent_success_regularContent() {
        ContentCommonDto common = ContentCommonDto.builder()
                .contentName("New Content")
                .contentType(ContentType.TEXT)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of("ch1"))
                .tags(List.of("tag1"))
                .build();

        ContentReqDto<Object> reqDto = ContentReqDto.<Object>builder()
                .common(common)
                .specific(null)
                .build();

        Topic topic = Topic.builder()
                .id("topic1")
                .totalContentCount(0)
                .build();

        when(contentRepository.existsByContentName("New Content")).thenReturn(false);
        when(chapterService.getChapterById("ch1")).thenReturn(ChapterResponseWithItemDto.builder().topicId("topic1").build());
        when(topicRepository.findById("topic1")).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentRespDto<?> response = contentService.createContent(reqDto);

        assertNotNull(response);
        assertEquals("New Content", response.getCommon().getContentName());
    }

    @Test
    void createContent_duplicateName_throws() {
        ContentCommonDto common = ContentCommonDto.builder()
                .contentName("Existing Content")
                .contentType(ContentType.TEXT)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of("ch1"))
                .tags(List.of("tag1"))
                .build();

        ContentReqDto<Object> reqDto = ContentReqDto.<Object>builder()
                .common(common)
                .build();

        when(contentRepository.existsByContentName("Existing Content")).thenReturn(true);

        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> contentService.createContent(reqDto));
        assertTrue(ex.getMessage().contains("Content already exists"));
    }
    @Test
    void updateContent_success_regularContent() {
        String contentId = "c1";
        Content existing = Content.builder()
                .id(contentId)
                .contentName("Old Content")
                .build();

        ContentCommonDto common = ContentCommonDto.builder()
                .contentName("New Content")
                .contentType(ContentType.TEXT)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of("ch1"))
                .tags(List.of("tag1"))
                .build();

        ContentReqDto<Object> reqDto = ContentReqDto.<Object>builder()
                .common(common)
                .specific(null)
                .build();

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(existing));
        when(contentRepository.existsByContentName("New Content")).thenReturn(false);
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentRespDto<?> result = contentService.updateContent(contentId, reqDto);

        assertNotNull(result);
        assertEquals("New Content", result.getCommon().getContentName());
    }

    @Test
    void updateContent_duplicateName_throws() {
        String contentId = "c1";
        Content existing = Content.builder()
                .id(contentId)
                .contentName("Old Content")
                .build();

        ContentCommonDto common = ContentCommonDto.builder()
                .contentName("Existing Content")
                .contentType(ContentType.TEXT)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of("ch1"))
                .tags(List.of("tag1"))
                .build();

        ContentReqDto<Object> reqDto = ContentReqDto.<Object>builder()
                .common(common)
                .build();

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(existing));
        when(contentRepository.existsByContentName("Existing Content")).thenReturn(true);

        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> contentService.updateContent(contentId, reqDto));
        assertTrue(ex.getMessage().contains("Content already exists"));
    }

    @Test
    void updateContent_notFound_throws() {
        when(contentRepository.findById("missing")).thenReturn(Optional.empty());

        ContentCommonDto common = ContentCommonDto.builder()
                .contentName("New Content")
                .contentType(ContentType.TEXT)
                .status(CommonStatus.PUBLISHED)
                .author("Author")
                .chapterIds(List.of("ch1"))
                .tags(List.of("tag1"))
                .build();

        ContentReqDto<Object> reqDto = ContentReqDto.<Object>builder()
                .common(common)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> contentService.updateContent("missing", reqDto));
    }


}


