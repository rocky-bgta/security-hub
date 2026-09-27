package com.aspire.asat.cms.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Tag;
import com.aspire.asat.cms.repository.TagRepository;
import com.aspire.asat.cms.util.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    private static final String USER_ID = "user-1";
    private static final String TAG_ID = "tag-1";

    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void createTag_savesActiveTagWithCreatedBy() {
        when(tagRepository.existsByNameIgnoreCase("Security")).thenReturn(false);
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().userId(USER_ID).build());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        TagRespDto result = tagService.createTag(TagReqDto.builder()
                .name(" Security ")
                .description("Security related")
                .build());

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(captor.capture());
        Tag saved = captor.getValue();

        assertEquals("Security", saved.getName());
        assertEquals(USER_ID, saved.getCreatedBy());
        assertEquals(Tag.STATUS_ACTIVE, saved.getStatus());
        assertEquals("Security", result.getName());
        assertEquals(Tag.STATUS_ACTIVE, result.getStatus());
    }

    @Test
    void createTag_throwsWhenNameExists() {
        when(tagRepository.existsByNameIgnoreCase("Security")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () ->
                tagService.createTag(TagReqDto.builder().name("Security").build()));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void getTagById_throwsWhenMissing() {
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tagService.getTagById(TAG_ID));
    }

    @Test
    void updateTagStatus_updatesStatus() {
        Tag existing = Tag.builder()
                .id(TAG_ID)
                .name("Security")
                .status(Tag.STATUS_ACTIVE)
                .createdAt(Instant.now())
                .createdBy(USER_ID)
                .build();
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(existing));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        TagRespDto result = tagService.updateTagStatus(TAG_ID, "inactive");

        assertEquals(Tag.STATUS_INACTIVE, result.getStatus());
        verify(tagRepository).save(existing);
    }

    @Test
    void updateTagStatus_rejectsInvalidStatus() {
        assertThrows(CmsServiceException.class, () -> tagService.updateTagStatus(TAG_ID, "ENABLED"));
        verify(tagRepository, never()).findById(any());
    }

    @Test
    void getAllTags_returnsSortedMappedList() {
        when(tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(
                Tag.builder().id("1").name("Alpha").status(Tag.STATUS_ACTIVE).build(),
                Tag.builder().id("2").name("Beta").status(Tag.STATUS_INACTIVE).build()
        ));

        List<TagRespDto> result = tagService.getAllTags();

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals("Beta", result.get(1).getName());
    }

    @Test
    void deleteTagById_setsStatusInactive() {
        Tag existing = Tag.builder()
                .id(TAG_ID)
                .name("Security")
                .status(Tag.STATUS_ACTIVE)
                .createdAt(Instant.now())
                .createdBy(USER_ID)
                .build();
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(existing));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        TagRespDto result = tagService.deleteTagById(TAG_ID);

        assertEquals(Tag.STATUS_INACTIVE, result.getStatus());
        verify(tagRepository).save(existing);
        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    void deleteTagById_throwsWhenMissing() {
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tagService.deleteTagById(TAG_ID));
        verify(tagRepository, never()).deleteById(any());
    }
}
