package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Chapter;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.ChapterService;
import com.aspire.asat.cms.util.ContentSpecificResponseNormalizer;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {
    private static final String CHAPTER_NOT_FOUND_WITH_ID = "Chapter not found with id: ";
    private final ChapterRepository chapterRepository;
    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChapterResponseDto saveChapter(ChapterRequestDto chapterDto) {
        String chapterName = chapterDto.getChapterName();
        String tId = chapterDto.getTopicId();

        // Check if the chapter already exists in the specific topic
        if (chapterRepository.existsByChapterNameAndTopicId(chapterName, tId)) {
            throw new DuplicateNameException("Chapter already exists with name: " + chapterName);
        }

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Chapter chapter = Chapter.toChapter(id.toString(), chapterDto, now, now);

        // Save the chapter to the repository
        Chapter savedChapter = chapterRepository.save(chapter);

        // Get the topicId from the chapter
        String topicId = chapterDto.getTopicId();

        // Fetch the topic from the repository
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        // Add the newly created chapterId to the topic's chapterIds list
        List<String> chapterIds = topic.getChapterIds();
        if (chapterIds == null) {
            chapterIds = new ArrayList<>();
        }
        chapterIds.add(savedChapter.getId());  // Add the new chapter ID to the list

        // Update the topic with the new chapterId
        topic.setChapterIds(chapterIds);
        topic.setUpdatedAt(Instant.now());

        // Save the updated topic
        topicRepository.save(topic);

        // Return the chapter response DTO
        return Chapter.toChapterDto(savedChapter);
    }

    @Override
    public List<ChapterResponseDto> getAllChapters(String search, String topicId, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<Chapter> pageChapters;
        if (search != null && !search.isEmpty()) {
            pageChapters = chapterRepository.findByChapterName(search, pageable);
        } else if (topicId != null) {
            pageChapters = chapterRepository.findByTopicId(topicId, pageable);
        } else {
            pageChapters = chapterRepository.findAll(pageable);
        }

        return pageChapters.getContent().stream()
                .map(Chapter::toChapterDto)
                .toList();
    }

    @Override
    public ChapterResponseWithItemDto getChapterById(String chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_WITH_ID + chapterId));
        List<ContentRespDto<?>> contentDetails = contentRepository.findAllById(chapter.getContentIds())
                .stream()
                .map(content -> ContentRespDto.builder()
                        .id(content.getId())
                        .common(ContentCommonDto.builder()
                                .contentName(content.getContentName())
                                .description(content.getDescription())
                                .contentType(content.getContentType())
                                .status(content.getStatus())
                                .author(content.getAuthor())
                                .chapterIds(content.getChapterIds() != null ? new ArrayList<>(content.getChapterIds()) : new ArrayList<>())
                                .tags(content.getTags() != null ? new ArrayList<>(content.getTags()) : new ArrayList<>())
                                .build())
                        .specific(ContentSpecificResponseNormalizer.normalizeForJsonResponse(objectMapper, content.getSpecificContent()))
                        .createdAt(content.getCreatedAt())
                        .updatedAt(content.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        return Chapter.toChapterDtoWithItemDetails(chapter, contentDetails);

    }

    @Override
    @Transactional
    public ChapterResponseDto updateChapterById(String chapterId, ChapterUpdateDto chapterDto) {
        return chapterRepository.findById(chapterId)
                .map(existingChapter -> {
                    log.info("ChapterServiceImpl.updateChapterById{}", existingChapter.getChapterName());

                    if (!existingChapter.getChapterName().equals(chapterDto.getChapterName())) {
                        boolean isChapterNameExists = chapterRepository.existsByChapterName(chapterDto.getChapterName());
                        log.info("ChapterServiceImpl.updateChapterById:{}", isChapterNameExists);
                        if (isChapterNameExists) {
                            throw new DuplicateNameException("Chapter already exists with name: " + chapterDto.getChapterName());
                        }
                    }

                    Chapter updatedChapter = Chapter.toUpdateChapter(chapterDto, chapterId, Instant.now());
                    updatedChapter.setCreatedAt(existingChapter.getCreatedAt());
                    log.info("**updated chapter object:{}", updatedChapter);
                    return Chapter.toChapterDto(chapterRepository.save(updatedChapter));
                })
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_WITH_ID + chapterId));
    }


    @Override
    public ChapterResponseDto deleteChapterById(String chapterId) {
        // Retrieve the chapter by its ID
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_WITH_ID + chapterId));

        // Get the list of contents associated with the chapter
        List<String> contentList = chapter.getContentIds();
        int numberOfContents = (contentList == null) ? 0 : contentList.size();

        // Fetch the related topic
        String topicId = chapter.getTopicId();
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        // Decrement the topic's totalContentCount safely
        Integer currentCount = topic.getTotalContentCount();
        if (currentCount == null) {
            currentCount = 0;
        }
        int updatedCount = currentCount - numberOfContents;
        if (updatedCount < 0) {
            updatedCount = 0;
        }
        topic.setTotalContentCount(updatedCount);
        topic.setUpdatedAt(Instant.now());
        topicRepository.save(topic);

        // Delete all contents assigned to this chapter
        if (contentList != null) {
            for (String contentId : contentList) {
                contentRepository.deleteById(contentId);
            }
        }

        // Now delete the chapter
        chapterRepository.delete(chapter);

        // Return the ChapterResponseDto of the deleted chapter
        return Chapter.toChapterDto(chapter);
    }

    @Override
    public List<Chapter> searchWithRelevance(String text) {
        return chapterRepository.findAllByChapterName(text);
    }

    @Override
    public void exportChaptersToCsv(Integer offset, Integer pageSize, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=chapters.csv");
        List<ChapterResponseDto> chapters = getAllChapters(null, null, offset, pageSize, "chapterName", "asc");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"ID", "Topic ID", "Chapter Name", "Chapter Description", "Position", "Chapter Status", "Content Type", "Created At", "Updated At"});
            for (ChapterResponseDto chapter : chapters) {
                writer.writeNext(new String[]{
                        chapter.getId(),
                        chapter.getTopicId(),
                        chapter.getChapterName(),
                        chapter.getChapterDescription(),
                        chapter.getPosition().toString(),
                        chapter.getChapterStatus().toString(),
                        chapter.getContentIds().toString(),
                        chapter.getCreatedAt().toString(),
                        chapter.getUpdatedAt().toString()});
            }
        } catch (Exception e) {
            log.error("Error exporting chapters to CSV", e);
            throw new CmsServiceException("Failed to export chapters to CSV");
        }

    }

    @Override
    public ChapterResponseDto updateChapterContentIds(String chapterId, List<String> newContentIds) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_WITH_ID + chapterId));

        List<String> existingContentIds = chapter.getContentIds();
        if (existingContentIds == null) {
            existingContentIds = new ArrayList<>();
        }
        existingContentIds.addAll(newContentIds);

        chapter.setContentIds(existingContentIds);
        chapter.setUpdatedAt(Instant.now());

        chapter = chapterRepository.save(chapter);

        return Chapter.toChapterDto(chapter);
    }

    @Override
    public long getTotalChapterCount(String topicId) {
        return chapterRepository.countByTopicId(topicId);
    }

    @Override
    public ChapterResponseDto removeContentFromChapter(String chapterId, String contentId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_WITH_ID + chapterId));

        List<String> contentIds = chapter.getContentIds();
        if (contentIds != null && contentIds.contains(contentId)) {
            contentIds.remove(contentId);
            chapter.setContentIds(contentIds);
            chapter.setUpdatedAt(Instant.now());
            chapter = chapterRepository.save(chapter);
        }
        return Chapter.toChapterDto(chapter);
    }

}
