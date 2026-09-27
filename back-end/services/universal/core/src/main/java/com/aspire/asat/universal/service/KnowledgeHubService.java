package com.aspire.asat.universal.service;

import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.universal.dto.TagDto;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.entity.Category;
import com.aspire.asat.universal.entity.KnowlegeHub;
import com.aspire.asat.universal.entity.KnowledgeHubLike;
import com.aspire.asat.universal.entity.KnowledgeHubTag;
import com.aspire.asat.universal.entity.Tag;
import com.aspire.asat.universal.repository.CategoryRepository;
import com.aspire.asat.universal.repository.KnowledgeHubRepository;
import com.aspire.asat.universal.repository.KnowledgeHubLikeRepository;
import com.aspire.asat.universal.repository.KnowledgeHubTagRepository;
import com.aspire.asat.universal.repository.TagRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;

@Service
@Data
@RequiredArgsConstructor
public class KnowledgeHubService {

    private final KnowledgeHubRepository knowledgeHubRepository;
    private final CategoryRepository categoryRepository;
    private final KnowledgeHubLikeRepository knowledgeHubLikeRepository;
    private final KnowledgeHubTagRepository knowledgeHubTagRepository;
    private final TagRepository tagRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ModelMapper modelMapper;
    private final KnowledgeHubCommentService knowledgeHubCommentService;

    public Page<KnowledgeHubDto> getAllKnowledge(Pageable pageable) {
        return knowledgeHubRepository.findAll(pageable)
                .map(knowledge -> convertToDto(knowledge, false));
    }

    public Page<KnowledgeHubDto> getActiveKnowledge(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return knowledgeHubRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                Status.ACTIVE, now, now, pageable)
                .map(knowledge -> convertToDto(knowledge, false));
    }

    // New: offset-based list (items only) + separate count methods
    public List<KnowledgeHubDto> getAllKnowledge(int offset, int pageSize) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        // Treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);
        return knowledgeHubRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(knowledge -> convertToDto(knowledge, false))
                .toList();
    }

    public long getAllKnowledgeCount() {
        return knowledgeHubRepository.count();
    }

    public List<KnowledgeHubDto> getActiveKnowledge(int offset, int pageSize) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        // Treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);
        LocalDateTime now = LocalDateTime.now();
        return knowledgeHubRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                Status.ACTIVE, now, now, pageable)
                .getContent()
                .stream()
                .map(knowledge -> convertToDto(knowledge, false))
                .toList();
    }

    public long getActiveKnowledgeCount() {
        LocalDateTime now = LocalDateTime.now();
        return knowledgeHubRepository.countByStatusAndPublishedDateBeforeAndExpireDateAfter(Status.ACTIVE, now, now);
    }

    // New: return OffsetPageDto for all knowledge with filters
    public OffsetPageDto<KnowledgeHubDto> getAllKnowledgePage(int offset, int pageSize, String search, String categoryId, String resourceType, String status) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);

        Page<KnowlegeHub> page = knowledgeHubRepository.findAllWithFilters(search, categoryId, resourceType, status, pageable);
        List<KnowledgeHubDto> items = page.getContent().stream()
                .map(knowledge -> convertToDto(knowledge, false))
                .toList();
        return new OffsetPageDto<>(offset, pageSize, page.getTotalElements(), items);
    }

    // New: return OffsetPageDto for active knowledge with filters
    public OffsetPageDto<KnowledgeHubDto> getActiveKnowledgePage(int offset, int pageSize, String search, String categoryId, String resourceType) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);
        LocalDateTime now = LocalDateTime.now();

        Page<KnowlegeHub> page = knowledgeHubRepository.findActiveWithFilters(search, categoryId, resourceType, now, pageable);
        List<KnowledgeHubDto> items = page.getContent().stream()
                .map(knowledge -> convertToDto(knowledge, false))
                .toList();
        return new OffsetPageDto<>(offset, pageSize, page.getTotalElements(), items);
    }


    public KnowledgeHubDto getKnowledgeById(String id) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            Optional<KnowlegeHub> knowledge = knowledgeHubRepository.findById(uuid);
            return knowledge.map(k -> convertToDto(k, true)).orElse(null);
        } catch (IllegalArgumentException ex) {
            // invalid UUID format
            return null;
        }
    }

    public KnowledgeHubDto getKnowledgeBySlug(String slug) {
        Optional<KnowlegeHub> knowledge = knowledgeHubRepository.findBySlug(slug);
        return knowledge.map(k -> convertToDto(k, true)).orElse(null);
    }

    public KnowledgeHubDto createKnowledge(KnowledgeHubRequest request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        // Validate slug uniqueness
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            Optional<KnowlegeHub> existingKnowledge = knowledgeHubRepository.findBySlug(request.getSlug());
            if (existingKnowledge.isPresent()) {
                throw new IllegalArgumentException("Knowledge with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Validate category exists and format
        if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
            throw new IllegalArgumentException("Category ID is required");
        }

        // Ensure categoryId is a valid ObjectId (prevents arbitrary strings from being treated as existing IDs)
        if (!ObjectId.isValid(request.getCategoryId())) {
            throw new IllegalArgumentException("Invalid category ID format: " + request.getCategoryId());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        // Manually map fields from request to entity to avoid accidental id/category collisions by ModelMapper
        KnowlegeHub knowledge = new KnowlegeHub();
        knowledge.setName(request.getName());
        knowledge.setSlug(request.getSlug());
        knowledge.setCategoryId(request.getCategoryId());
        knowledge.setContent(request.getContent());
        knowledge.setSequence(request.getSequence());
        knowledge.setImageUrl(request.getImageUrl());
        knowledge.setVideoUrl(request.getVideoUrl());
        knowledge.setPublishedDate(request.getPublishedDate());
        knowledge.setExpireDate(request.getExpireDate());
        knowledge.setStatus(request.getStatus());
        knowledge.setResourceType(request.getResourceType());
        knowledge.setAllowDownload(request.isAllowDownload());
        // attach resolved category
        knowledge.setCategory(category);
        knowledge.setCreatedBy(context.getUserId());
        knowledge.setCreatedAt(LocalDateTime.now());
        knowledge.setUpdatedBy(context.getUserId());
        knowledge.setUpdatedAt(LocalDateTime.now());
        knowledge.setLikeCount(0);
        knowledge.setDislikeCount(0);

        KnowlegeHub savedKnowledge = knowledgeHubRepository.save(knowledge);

        // Handle tags - split comma separated values and save individually
        if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
            saveTags(savedKnowledge.getId(), request.getTags());
        }

        return convertToDto(savedKnowledge, true);
    }

    public KnowledgeHubDto updateKnowledge(String id, KnowledgeHubRequest request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        if (UserTypeUtils.isMspAdmin(context.getUserType())) {
            throw new IllegalArgumentException("Knowledge hub cannot be updated by MSP");
        }

        // Validate slug uniqueness (if changing the slug)
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            Optional<KnowlegeHub> existingKnowledgeBySlug = knowledgeHubRepository.findBySlug(request.getSlug());
            if (existingKnowledgeBySlug.isPresent() && !existingKnowledgeBySlug.get().getId().toString().equals(id)) {
                throw new IllegalArgumentException("Knowledge with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Validate category exists
        if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
            throw new IllegalArgumentException("Category ID is required");
        }
        if (!ObjectId.isValid(request.getCategoryId())) {
            throw new IllegalArgumentException("Invalid category ID format: " + request.getCategoryId());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            Optional<KnowlegeHub> existingKnowledgeOptional = knowledgeHubRepository.findById(uuid);
            if (existingKnowledgeOptional.isPresent()) {
                KnowlegeHub existingKnowledge = existingKnowledgeOptional.get();

                // Manually update fields from request (avoid ModelMapper to prevent id collisions)
                existingKnowledge.setName(request.getName());
                existingKnowledge.setSlug(request.getSlug());
                existingKnowledge.setCategoryId(request.getCategoryId());
                existingKnowledge.setContent(request.getContent());
                existingKnowledge.setSequence(request.getSequence());
                existingKnowledge.setImageUrl(request.getImageUrl());
                existingKnowledge.setVideoUrl(request.getVideoUrl());
                existingKnowledge.setPublishedDate(request.getPublishedDate());
                existingKnowledge.setExpireDate(request.getExpireDate());
                existingKnowledge.setStatus(request.getStatus());
                existingKnowledge.setResourceType(request.getResourceType());
                existingKnowledge.setAllowDownload(request.isAllowDownload());
                existingKnowledge.setCategory(category);

                //existingNews.setId(id); // Ensure ID is not changed
                existingKnowledge.setUpdatedBy(context.getUserId());
                existingKnowledge.setUpdatedAt(LocalDateTime.now());

                KnowlegeHub updatedKnowledge = knowledgeHubRepository.save(existingKnowledge);

                // Handle tags - delete existing tags and save new ones
                knowledgeHubTagRepository.deleteByKnowledgeHubId(uuid);
                if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
                    saveTags(uuid, request.getTags());
                }

                return convertToDto(updatedKnowledge, true);
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return null;
    }

    public boolean deleteKnowledge(String id) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            if (knowledgeHubRepository.existsById(uuid)) {
                // Delete associated tags first
                knowledgeHubTagRepository.deleteByKnowledgeHubId(uuid);
                knowledgeHubRepository.deleteById(uuid);
                return true;
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return false;
    }

    public KnowledgeHubDto updateKnowledgeSequence(NewsSequenceRequest request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            java.util.UUID uuid = java.util.UUID.fromString(request.getId());
            Optional<KnowlegeHub> existingKnowledgeOptional = knowledgeHubRepository.findById(uuid);
            if (existingKnowledgeOptional.isPresent()) {
                KnowlegeHub existingKnowledge = existingKnowledgeOptional.get();
                existingKnowledge.setSequence(request.getSequence());
                existingKnowledge.setUpdatedBy(context.getUserId());
                existingKnowledge.setUpdatedAt(LocalDateTime.now());

                KnowlegeHub updatedKnowledge = knowledgeHubRepository.save(existingKnowledge);
                return convertToDto(updatedKnowledge, true);
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return null;
    }

    public KnowledgeHubDto likeKnowledge(String knowledgeId, boolean liked) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();
        try {
            java.util.UUID uuid = java.util.UUID.fromString(knowledgeId);
            Optional<KnowlegeHub> knowledgeOptional = knowledgeHubRepository.findById(uuid);
            if (knowledgeOptional.isEmpty()) {
                return null;
            }

            KnowlegeHub knowledge = knowledgeOptional.get();
            Optional<KnowledgeHubLike> existingLikeOptional = knowledgeHubLikeRepository.findByKnowledgeHubIdAndUserId(knowledgeId, userId);

            if (existingLikeOptional.isPresent()) {
                KnowledgeHubLike existingLike = existingLikeOptional.get();

                // If the action is the same, no change needed
                if (existingLike.isLiked() == liked) {
                    return convertToDto(knowledge, false);
                }

                // Remove previous like/dislike count
                if (existingLike.isLiked()) {
                    knowledge.setLikeCount(Math.max(0, knowledge.getLikeCount() - 1));
                } else {
                    knowledge.setDislikeCount(Math.max(0, knowledge.getDislikeCount() - 1));
                }

                // Update with new preference
                existingLike.setLiked(liked);
                existingLike.setCreatedAt(LocalDateTime.now());
                knowledgeHubLikeRepository.save(existingLike);

            } else {
                // Create new like record
                KnowledgeHubLike newLike = new KnowledgeHubLike();
                newLike.setKnowledgeHubId(knowledgeId);
                newLike.setUserId(userId);
                newLike.setLiked(liked);
                newLike.setCreatedAt(LocalDateTime.now());
                knowledgeHubLikeRepository.save(newLike);
            }

            // Update the count
            if (liked) {
                knowledge.setLikeCount(knowledge.getLikeCount() + 1);
            } else {
                knowledge.setDislikeCount(knowledge.getDislikeCount() + 1);
            }

            KnowlegeHub updatedKnowledge = knowledgeHubRepository.save(knowledge);
            return convertToDto(updatedKnowledge, false);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // Update only the status field; kept as a separate method so controllers can update status without providing full payload
    public KnowledgeHubDto updateKnowledgeStatus(String id, Status status) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            Optional<KnowlegeHub> existingKnowledgeOptional = knowledgeHubRepository.findById(uuid);
            if (existingKnowledgeOptional.isPresent()) {
                KnowlegeHub existingKnowledge = existingKnowledgeOptional.get();
                existingKnowledge.setStatus(status);
                existingKnowledge.setUpdatedBy(context != null ? context.getUserId() : null);
                existingKnowledge.setUpdatedAt(LocalDateTime.now());
                KnowlegeHub updated = knowledgeHubRepository.save(existingKnowledge);
                return convertToDto(updated, false);
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return null;
    }

    /**
     * Helper method to save tags by splitting comma-separated values
     * Creates tags in the tags table if they don't exist, then links them to knowledge hub
     */
    private void saveTags(java.util.UUID knowledgeHubId, String tags) {
        if (tags != null && !tags.trim().isEmpty()) {
            CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
            String[] tagArray = tags.split(",");
            for (String tagName : tagArray) {
                String trimmedTagName = tagName.trim();
                if (!trimmedTagName.isEmpty()) {
                    // Find or create tag
                    Tag tag = tagRepository.findByName(trimmedTagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag(trimmedTagName, context.getUserId());
                                return tagRepository.save(newTag);
                            });

                    // Create knowledge hub tag relationship
                    KnowledgeHubTag knowledgeHubTag = new KnowledgeHubTag(knowledgeHubId, tag.getId());
                    knowledgeHubTag.setTag(tag);
                    knowledgeHubTagRepository.save(knowledgeHubTag);
                }
            }
        }
    }

    /**
     * Helper method to get tags for a knowledge hub
     */
    private List<TagDto> getTags(java.util.UUID knowledgeHubId) {
        List<KnowledgeHubTag> knowledgeHubTags = knowledgeHubTagRepository.findByKnowledgeHubId(knowledgeHubId);
        return knowledgeHubTags.stream()
                .filter(kht -> kht.getTag() != null)
                .map(kht -> modelMapper.map(kht.getTag(), TagDto.class))
                .toList();
    }

    private KnowledgeHubDto convertToDto(KnowlegeHub knowledge, boolean includeComments) {
        KnowledgeHubDto dto = new KnowledgeHubDto();

        // Manually map all fields to avoid ModelMapper conflicts
        dto.setId(knowledge.getId());
        dto.setName(knowledge.getName());
        dto.setSlug(knowledge.getSlug());
        dto.setCategoryId(knowledge.getCategoryId());
        dto.setContent(knowledge.getContent());
        dto.setSequence(knowledge.getSequence());
        dto.setImageUrl(knowledge.getImageUrl());
        dto.setVideoUrl(knowledge.getVideoUrl());
        dto.setPublishedDate(knowledge.getPublishedDate());
        dto.setExpireDate(knowledge.getExpireDate());
        dto.setStatus(knowledge.getStatus());
        dto.setLikeCount(knowledge.getLikeCount());
        dto.setDislikeCount(knowledge.getDislikeCount());
        dto.setResourceType(knowledge.getResourceType());
        dto.setAllowDownload(knowledge.isAllowDownload());

        // Map category if available
        if (knowledge.getCategory() != null) {
            dto.setCategory(modelMapper.map(knowledge.getCategory(), CategoryDto.class));
        }

        // Get tags for this knowledge hub
        dto.setTags(getTags(knowledge.getId()));

        if (includeComments && knowledge.getId() != null) {
            dto.setComments(knowledgeHubCommentService.getCommentsForKnowledgeHub(knowledge.getId()));
        }

        return dto;
    }
}
