package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.entity.Category;
import com.aspire.asat.universal.entity.KnowlegeHub;
import com.aspire.asat.universal.entity.KnowledgeHubLike;
import com.aspire.asat.universal.entity.KnowledgeHubTag;
import com.aspire.asat.universal.entity.Tag;
import com.aspire.asat.universal.enums.ResourceType;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.repository.CategoryRepository;
import com.aspire.asat.universal.repository.KnowledgeHubLikeRepository;
import com.aspire.asat.universal.repository.KnowledgeHubRepository;
import com.aspire.asat.universal.repository.KnowledgeHubTagRepository;
import com.aspire.asat.universal.repository.TagRepository;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeHubServiceTest {

    private static final String VALID_CATEGORY_ID = new ObjectId().toHexString();
    private static final String USER_ID = "admin-user-id";

    @Mock
    private KnowledgeHubRepository knowledgeHubRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private KnowledgeHubLikeRepository knowledgeHubLikeRepository;
    @Mock
    private KnowledgeHubTagRepository knowledgeHubTagRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private KnowledgeHubCommentService knowledgeHubCommentService;

    @InjectMocks
    private KnowledgeHubService knowledgeHubService;

    private UUID knowledgeId;
    private KnowlegeHub knowledgeHub;
    private Category category;
    private CurrentUserContext adminContext;

    @BeforeEach
    void setUp() {
        knowledgeId = UUID.randomUUID();
        category = new Category();
        category.setId(VALID_CATEGORY_ID);
        category.setName("Security");

        knowledgeHub = buildKnowledgeHub(knowledgeId, "resource-slug");

        adminContext = CurrentUserContext.builder()
                .userId(USER_ID)
                .userType("ASPIRE_ADMIN")
                .build();

        stubDtoDependencies();
    }

    private void stubDtoDependencies() {
        lenient().when(knowledgeHubTagRepository.findByKnowledgeHubId(any())).thenReturn(Collections.emptyList());
        lenient().when(knowledgeHubCommentService.getCommentsForKnowledgeHub(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void getAllKnowledge_withPageable_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findAll(pageable)).thenReturn(page);

        Page<KnowledgeHubDto> result = knowledgeHubService.getAllKnowledge(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(knowledgeId, result.getContent().get(0).getId());
        verify(knowledgeHubRepository).findAll(pageable);
    }

    @Test
    void getActiveKnowledge_withPageable_returnsActiveItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                eq(Status.ACTIVE), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        Page<KnowledgeHubDto> result = knowledgeHubService.getActiveKnowledge(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Sample Resource", result.getContent().get(0).getName());
    }

    @Test
    void getAllKnowledge_withOffsetAndPageSize_returnsItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findAll(pageable)).thenReturn(page);

        List<KnowledgeHubDto> result = knowledgeHubService.getAllKnowledge(0, 10);

        assertEquals(1, result.size());
        assertEquals(knowledgeId, result.get(0).getId());
    }

    @Test
    void getAllKnowledge_normalizesInvalidOffsetAndPageSize() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findAll(pageable)).thenReturn(page);

        List<KnowledgeHubDto> result = knowledgeHubService.getAllKnowledge(-5, 0);

        assertEquals(1, result.size());
        verify(knowledgeHubRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getAllKnowledgeCount_returnsRepositoryCount() {
        when(knowledgeHubRepository.count()).thenReturn(12L);

        assertEquals(12L, knowledgeHubService.getAllKnowledgeCount());
    }

    @Test
    void getActiveKnowledge_withOffsetAndPageSize_returnsItems() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                eq(Status.ACTIVE), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        List<KnowledgeHubDto> result = knowledgeHubService.getActiveKnowledge(1, 5);

        assertEquals(1, result.size());
    }

    @Test
    void getActiveKnowledgeCount_returnsRepositoryCount() {
        when(knowledgeHubRepository.countByStatusAndPublishedDateBeforeAndExpireDateAfter(
                eq(Status.ACTIVE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(7L);

        assertEquals(7L, knowledgeHubService.getActiveKnowledgeCount());
    }

    @Test
    void getAllKnowledgePage_returnsOffsetPageDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findAllWithFilters("search", VALID_CATEGORY_ID, "PDF", "ACTIVE", pageable))
                .thenReturn(page);

        OffsetPageDto<KnowledgeHubDto> result = knowledgeHubService.getAllKnowledgePage(
                0, 10, "search", VALID_CATEGORY_ID, "PDF", "ACTIVE");

        assertEquals(0, result.getOffset());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getActiveKnowledgePage_returnsOffsetPageDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<KnowlegeHub> page = new PageImpl<>(List.of(knowledgeHub), pageable, 1);
        when(knowledgeHubRepository.findActiveWithFilters(
                eq("search"), eq(VALID_CATEGORY_ID), eq("PDF"), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        OffsetPageDto<KnowledgeHubDto> result = knowledgeHubService.getActiveKnowledgePage(
                0, 10, "search", VALID_CATEGORY_ID, "PDF");

        assertEquals(1, result.getTotal());
        assertEquals(knowledgeId, result.getItems().get(0).getId());
    }

    @Test
    void getKnowledgeById_whenFound_returnsDtoWithComments() {
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));

        KnowledgeHubDto result = knowledgeHubService.getKnowledgeById(knowledgeId.toString());

        assertNotNull(result);
        assertEquals(knowledgeId, result.getId());
        verify(knowledgeHubCommentService).getCommentsForKnowledgeHub(knowledgeId);
    }

    @Test
    void getKnowledgeById_whenNotFound_returnsNull() {
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.getKnowledgeById(knowledgeId.toString()));
    }

    @Test
    void getKnowledgeById_whenInvalidUuid_returnsNull() {
        assertNull(knowledgeHubService.getKnowledgeById("not-a-uuid"));
        verify(knowledgeHubRepository, never()).findById(any());
    }

    @Test
    void getKnowledgeBySlug_whenFound_returnsDto() {
        when(knowledgeHubRepository.findBySlug("resource-slug")).thenReturn(Optional.of(knowledgeHub));

        KnowledgeHubDto result = knowledgeHubService.getKnowledgeBySlug("resource-slug");

        assertNotNull(result);
        assertEquals("resource-slug", result.getSlug());
    }

    @Test
    void getKnowledgeBySlug_whenNotFound_returnsNull() {
        when(knowledgeHubRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.getKnowledgeBySlug("missing"));
    }

    @Test
    void createKnowledge_whenValidRequest_savesAndReturnsDto() {
        KnowledgeHubRequest request = buildRequest("new-slug");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("new-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.of(category));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> {
            KnowlegeHub saved = invocation.getArgument(0);
            saved.setId(knowledgeId);
            return saved;
        });
        when(modelMapper.map(category, CategoryDto.class)).thenReturn(new CategoryDto());

        KnowledgeHubDto result = knowledgeHubService.createKnowledge(request);

        assertNotNull(result);
        assertEquals(knowledgeId, result.getId());
        verify(knowledgeHubRepository).save(any(KnowlegeHub.class));
    }

    @Test
    void createKnowledge_whenTagsProvided_savesTagRelationships() {
        KnowledgeHubRequest request = buildRequest("tagged-slug");
        request.setTags("alpha, beta");
        Tag tag = new Tag("alpha", USER_ID);
        tag.setId(UUID.randomUUID());

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("tagged-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.of(category));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> {
            KnowlegeHub saved = invocation.getArgument(0);
            saved.setId(knowledgeId);
            return saved;
        });
        when(tagRepository.findByName("alpha")).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("beta")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag savedTag = invocation.getArgument(0);
            savedTag.setId(UUID.randomUUID());
            return savedTag;
        });
        when(modelMapper.map(category, CategoryDto.class)).thenReturn(new CategoryDto());

        KnowledgeHubDto result = knowledgeHubService.createKnowledge(request);

        assertNotNull(result);
        verify(knowledgeHubTagRepository, org.mockito.Mockito.atLeastOnce()).save(any(KnowledgeHubTag.class));
    }

    @Test
    void createKnowledge_whenSlugAlreadyExists_throwsException() {
        KnowledgeHubRequest request = buildRequest("duplicate-slug");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("duplicate-slug")).thenReturn(Optional.of(knowledgeHub));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.createKnowledge(request)
        );

        assertEquals("Knowledge with slug 'duplicate-slug' already exists", exception.getMessage());
    }

    @Test
    void createKnowledge_whenCategoryMissing_throwsException() {
        KnowledgeHubRequest request = buildRequest("no-category");
        request.setCategoryId(null);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.createKnowledge(request)
        );

        assertEquals("Category ID is required", exception.getMessage());
    }

    @Test
    void createKnowledge_whenCategoryIdInvalidFormat_throwsException() {
        KnowledgeHubRequest request = buildRequest("bad-category");
        request.setCategoryId("invalid-id");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.createKnowledge(request)
        );

        assertTrue(exception.getMessage().contains("Invalid category ID format"));
    }

    @Test
    void createKnowledge_whenCategoryNotFound_throwsException() {
        KnowledgeHubRequest request = buildRequest("missing-category");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.createKnowledge(request)
        );

        assertEquals("Invalid category ID: " + VALID_CATEGORY_ID, exception.getMessage());
    }

    @Test
    void updateKnowledge_whenMspUser_throwsException() {
        KnowledgeHubRequest request = buildRequest("resource-slug");
        CurrentUserContext mspContext = CurrentUserContext.builder()
                .userId("msp-user-id")
                .userType("MSP")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.updateKnowledge(knowledgeId.toString(), request)
        );

        assertEquals("Knowledge hub cannot be updated by MSP", exception.getMessage());
        verify(knowledgeHubRepository, never()).save(any());
    }

    @Test
    void updateKnowledge_whenValidRequest_updatesAndReturnsDto() {
        KnowledgeHubRequest request = buildRequest("resource-slug");
        request.setName("Updated Resource");

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("resource-slug")).thenReturn(Optional.of(knowledgeHub));
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.of(category));
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(category, CategoryDto.class)).thenReturn(new CategoryDto());

        KnowledgeHubDto result = knowledgeHubService.updateKnowledge(knowledgeId.toString(), request);

        assertNotNull(result);
        assertEquals("Updated Resource", result.getName());
        verify(knowledgeHubTagRepository).deleteByKnowledgeHubId(knowledgeId);
        verify(knowledgeHubRepository).save(knowledgeHub);
    }

    @Test
    void updateKnowledge_whenSlugUsedByAnotherResource_throwsException() {
        UUID otherId = UUID.randomUUID();
        KnowledgeHubRequest request = buildRequest("other-slug");
        KnowlegeHub otherKnowledge = buildKnowledgeHub(otherId, "other-slug");

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("other-slug")).thenReturn(Optional.of(otherKnowledge));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> knowledgeHubService.updateKnowledge(knowledgeId.toString(), request)
        );

        assertEquals("Knowledge with slug 'other-slug' already exists", exception.getMessage());
    }

    @Test
    void updateKnowledge_whenNotFound_returnsNull() {
        KnowledgeHubRequest request = buildRequest("resource-slug");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("resource-slug")).thenReturn(Optional.of(knowledgeHub));
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.of(category));
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.updateKnowledge(knowledgeId.toString(), request));
    }

    @Test
    void updateKnowledge_whenInvalidUuid_returnsNull() {
        KnowledgeHubRequest request = buildRequest("resource-slug");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findBySlug("resource-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findById(VALID_CATEGORY_ID)).thenReturn(Optional.of(category));

        assertNull(knowledgeHubService.updateKnowledge("invalid-uuid", request));
    }

    @Test
    void deleteKnowledge_whenExists_deletesTagsAndResource() {
        when(knowledgeHubRepository.existsById(knowledgeId)).thenReturn(true);

        boolean deleted = knowledgeHubService.deleteKnowledge(knowledgeId.toString());

        assertTrue(deleted);
        verify(knowledgeHubTagRepository).deleteByKnowledgeHubId(knowledgeId);
        verify(knowledgeHubRepository).deleteById(knowledgeId);
    }

    @Test
    void deleteKnowledge_whenNotExists_returnsFalse() {
        when(knowledgeHubRepository.existsById(knowledgeId)).thenReturn(false);

        assertFalse(knowledgeHubService.deleteKnowledge(knowledgeId.toString()));
        verify(knowledgeHubRepository, never()).deleteById(any());
    }

    @Test
    void deleteKnowledge_whenInvalidUuid_returnsFalse() {
        assertFalse(knowledgeHubService.deleteKnowledge("invalid-uuid"));
    }

    @Test
    void updateKnowledgeSequence_whenFound_updatesSequence() {
        NewsSequenceRequest request = new NewsSequenceRequest(knowledgeId.toString(), 5);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeHubDto result = knowledgeHubService.updateKnowledgeSequence(request);

        assertNotNull(result);
        assertEquals(5, result.getSequence());
    }

    @Test
    void updateKnowledgeSequence_whenNotFound_returnsNull() {
        NewsSequenceRequest request = new NewsSequenceRequest(knowledgeId.toString(), 5);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.updateKnowledgeSequence(request));
    }

    @Test
    void updateKnowledgeSequence_whenInvalidUuid_returnsNull() {
        NewsSequenceRequest request = new NewsSequenceRequest("invalid-uuid", 5);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);

        assertNull(knowledgeHubService.updateKnowledgeSequence(request));
    }

    @Test
    void likeKnowledge_whenNewLike_incrementsLikeCount() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubLikeRepository.findByKnowledgeHubIdAndUserId(knowledgeId.toString(), USER_ID))
                .thenReturn(Optional.empty());
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeHubDto result = knowledgeHubService.likeKnowledge(knowledgeId.toString(), true);

        assertNotNull(result);
        assertEquals(1, result.getLikeCount());
        verify(knowledgeHubLikeRepository).save(any(KnowledgeHubLike.class));
    }

    @Test
    void likeKnowledge_whenSameActionAlreadyTaken_returnsWithoutChangingCounts() {
        knowledgeHub.setLikeCount(3);
        KnowledgeHubLike existingLike = new KnowledgeHubLike();
        existingLike.setLiked(true);

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubLikeRepository.findByKnowledgeHubIdAndUserId(knowledgeId.toString(), USER_ID))
                .thenReturn(Optional.of(existingLike));

        KnowledgeHubDto result = knowledgeHubService.likeKnowledge(knowledgeId.toString(), true);

        assertNotNull(result);
        assertEquals(3, result.getLikeCount());
        verify(knowledgeHubRepository, never()).save(any());
    }

    @Test
    void likeKnowledge_whenSwitchingFromLikeToDislike_updatesCounts() {
        knowledgeHub.setLikeCount(2);
        knowledgeHub.setDislikeCount(1);
        KnowledgeHubLike existingLike = new KnowledgeHubLike();
        existingLike.setLiked(true);

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubLikeRepository.findByKnowledgeHubIdAndUserId(knowledgeId.toString(), USER_ID))
                .thenReturn(Optional.of(existingLike));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeHubDto result = knowledgeHubService.likeKnowledge(knowledgeId.toString(), false);

        assertNotNull(result);
        assertEquals(1, result.getLikeCount());
        assertEquals(2, result.getDislikeCount());
    }

    @Test
    void likeKnowledge_whenNotFound_returnsNull() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.likeKnowledge(knowledgeId.toString(), true));
    }

    @Test
    void likeKnowledge_whenInvalidUuid_returnsNull() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);

        assertNull(knowledgeHubService.likeKnowledge("invalid-uuid", true));
    }

    @Test
    void updateKnowledgeStatus_whenFound_updatesStatus() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.of(knowledgeHub));
        when(knowledgeHubRepository.save(any(KnowlegeHub.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeHubDto result = knowledgeHubService.updateKnowledgeStatus(knowledgeId.toString(), Status.INACTIVE);

        assertNotNull(result);
        assertEquals(Status.INACTIVE, result.getStatus());
        assertEquals(USER_ID, knowledgeHub.getUpdatedBy());
    }

    @Test
    void updateKnowledgeStatus_whenNotFound_returnsNull() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);
        when(knowledgeHubRepository.findById(knowledgeId)).thenReturn(Optional.empty());

        assertNull(knowledgeHubService.updateKnowledgeStatus(knowledgeId.toString(), Status.ACTIVE));
    }

    @Test
    void updateKnowledgeStatus_whenInvalidUuid_returnsNull() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminContext);

        assertNull(knowledgeHubService.updateKnowledgeStatus("invalid-uuid", Status.ACTIVE));
    }

    private KnowledgeHubRequest buildRequest(String slug) {
        KnowledgeHubRequest request = new KnowledgeHubRequest();
        request.setName("Sample Resource");
        request.setSlug(slug);
        request.setCategoryId(VALID_CATEGORY_ID);
        request.setContent("Content body");
        request.setSequence(1);
        request.setStatus(Status.ACTIVE);
        request.setResourceType(ResourceType.PDF);
        request.setAllowDownload(true);
        request.setPublishedDate(LocalDateTime.now().minusDays(1));
        request.setExpireDate(LocalDateTime.now().plusDays(30));
        return request;
    }

    private KnowlegeHub buildKnowledgeHub(UUID id, String slug) {
        KnowlegeHub entity = new KnowlegeHub();
        entity.setId(id);
        entity.setName("Sample Resource");
        entity.setSlug(slug);
        entity.setCategoryId(VALID_CATEGORY_ID);
        entity.setContent("Content body");
        entity.setSequence(1);
        entity.setStatus(Status.ACTIVE);
        entity.setResourceType(ResourceType.PDF);
        entity.setAllowDownload(true);
        entity.setLikeCount(0);
        entity.setDislikeCount(0);
        entity.setCategory(category);
        entity.setPublishedDate(LocalDateTime.now().minusDays(1));
        entity.setExpireDate(LocalDateTime.now().plusDays(30));
        return entity;
    }
}
