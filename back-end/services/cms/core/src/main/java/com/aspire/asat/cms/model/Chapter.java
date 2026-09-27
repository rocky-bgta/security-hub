package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.enums.ChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    private String id;
    private String topicId;
    private String chapterName;
    private String chapterDescription;
    private Integer position;
    private ChapterStatus chapterStatus;
    private List<String> contentIds;
    private Instant createdAt;
    private Instant updatedAt;

    public static ChapterResponseDto toChapterDto(Chapter chapter) {
        return ChapterResponseDto.builder().
                id(chapter.getId()).
                topicId(chapter.getTopicId()).
                chapterName(chapter.getChapterName()).
                chapterDescription(chapter.getChapterDescription()).
                position(chapter.getPosition()).
                chapterStatus(chapter.getChapterStatus()).
                contentIds(chapter.getContentIds()).
                createdAt(chapter.getCreatedAt()).
                updatedAt(chapter.getUpdatedAt()).
                build();
    }

    public static ChapterResponseWithItemDto toChapterDtoWithItemDetails(Chapter chapter, List<ContentRespDto<?>> contentDetailDto) {
        return ChapterResponseWithItemDto.builder().
                id(chapter.getId()).
                topicId(chapter.getTopicId()).
                chapterName(chapter.getChapterName()).
                chapterDescription(chapter.getChapterDescription()).
                position(chapter.getPosition()).
                chapterStatus(chapter.getChapterStatus()).
                contentIds(contentDetailDto).
                createdAt(chapter.getCreatedAt()).
                updatedAt(chapter.getUpdatedAt()).
                build();
    }


    public static Chapter toChapter(String chapterId, ChapterRequestDto chapterDto, Instant createdAt, Instant updatedAt) {
        return Chapter.builder().
                id(chapterId).
                topicId(chapterDto.getTopicId()).
                chapterName(chapterDto.getChapterName()).
                chapterDescription(chapterDto.getChapterDescription()).
                position(chapterDto.getPosition()).
                chapterStatus(chapterDto.getChapterStatus()).
                contentIds(chapterDto.getContentIds()).
                createdAt(createdAt).
                updatedAt(updatedAt).
                build();
    }

    public static Chapter toUpdateChapter(ChapterUpdateDto chapterDto, String chapterId, Instant updatedAt) {
        return Chapter.builder().
                id(chapterId).
                topicId(chapterDto.getTopicId()).
                chapterName(chapterDto.getChapterName()).
                chapterDescription(chapterDto.getChapterDescription()).
                position(chapterDto.getPosition()).
                chapterStatus(chapterDto.getChapterStatus()).
                contentIds(chapterDto.getContentIds()).
                updatedAt(updatedAt).
                build();
    }
}
