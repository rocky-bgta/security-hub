package com.aspire.asat.cms.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Tag;
import com.aspire.asat.cms.repository.TagRepository;
import com.aspire.asat.cms.service.TagService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(Tag.STATUS_ACTIVE, Tag.STATUS_INACTIVE);

    private final TagRepository tagRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public TagRespDto createTag(TagReqDto request) {
        log.info("Creating tag: {}", request.getName());
        String name = request.getName().trim();
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Tag with name '" + name + "' already exists.");
        }

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        String createdBy = currentUser != null ? currentUser.getUserId() : null;

        Tag saved = tagRepository.save(Tag.toEntity(request, createdBy));
        return Tag.toRespDto(saved);
    }

    @Override
    public TagRespDto updateTag(String id, TagReqDto request) {
        log.info("Updating tag id={}", id);
        Tag existing = findTagOrThrow(id);

        String name = request.getName().trim();
        if (!existing.getName().equalsIgnoreCase(name) && tagRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Tag with name '" + name + "' already exists.");
        }

        existing.setName(name);
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        return Tag.toRespDto(tagRepository.save(existing));
    }

    @Override
    public TagRespDto getTagById(String id) {
        log.info("Fetching tag id={}", id);
        return Tag.toRespDto(findTagOrThrow(id));
    }

    @Override
    public TagRespDto deleteTagById(String id) {
        log.info("Soft-deleting tag id={} by setting status to INACTIVE", id);
        return updateTagStatus(id, Tag.STATUS_INACTIVE);
    }

    @Override
    public List<TagRespDto> getAllTags() {
        log.info("Fetching all tags");
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(Tag::toRespDto)
                .toList();
    }

    @Override
    public TagRespDto updateTagStatus(String id, String status) {
        log.info("Updating tag status id={}, status={}", id, status);
        String normalized = normalizeStatus(status);
        Tag existing = findTagOrThrow(id);
        existing.setStatus(normalized);
        return Tag.toRespDto(tagRepository.save(existing));
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new CmsServiceException("Status is required", HttpStatus.BAD_REQUEST);
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new CmsServiceException("Status must be ACTIVE or INACTIVE", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private Tag findTagOrThrow(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with id '" + id + "' not found."));
    }
}
