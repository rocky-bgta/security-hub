package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.model.Chapter;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;


public interface ChapterService {
    ChapterResponseDto removeContentFromChapter(String chapterId, String contentId);
    ChapterResponseDto saveChapter(ChapterRequestDto chapterDto);
    List<ChapterResponseDto> getAllChapters(String search, String topicId, Integer offset, Integer pageSize, String sortBy, String order);
    ChapterResponseWithItemDto getChapterById(String chapterId);
    ChapterResponseDto updateChapterById(String chapterId, ChapterUpdateDto chapterDto);
    ChapterResponseDto deleteChapterById(String chapterId);
    List<Chapter> searchWithRelevance(String text);
    void exportChaptersToCsv(Integer offset, Integer pageSize, HttpServletResponse response);
    long getTotalChapterCount(String topicId);
    ChapterResponseDto updateChapterContentIds(String chapterId, List<String> contentIds);
}
