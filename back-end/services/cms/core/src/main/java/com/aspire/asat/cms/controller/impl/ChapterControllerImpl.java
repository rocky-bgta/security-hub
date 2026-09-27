package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ChapterController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.model.Chapter;
import com.aspire.asat.cms.service.ChapterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class ChapterControllerImpl implements ChapterController {

    private final ChapterService chapterService;

    @Autowired
    public ChapterControllerImpl(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ChapterResponseDto>> createChapter(ChapterRequestDto chapterDto) {
        ChapterResponseDto savedChapter = chapterService.saveChapter(chapterDto);
        ApiResponseDto<ChapterResponseDto> response = new ApiResponseDto<>("Chapter created successfully", 201, savedChapter);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ChapterResponseDto>>>> getAllChapters(String search, String topicId, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ChapterResponseDto> chapters = chapterService.getAllChapters(search, topicId, offset, pageSize, sortBy, order);
        AllResponseDto<List<ChapterResponseDto>> allResponseDto = new AllResponseDto<>(offset, pageSize, chapterService.getTotalChapterCount(topicId), chapters);
        ApiResponseDto<AllResponseDto<List<ChapterResponseDto>>> response = new ApiResponseDto<>("Chapters fetched successfully", 200, allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<ApiResponseDto<ChapterResponseWithItemDto>> getChapterById(String chapterId) {
        ChapterResponseWithItemDto chapter = chapterService.getChapterById(chapterId);
        ApiResponseDto<ChapterResponseWithItemDto> response = new ApiResponseDto<>("Chapter fetched successfully", 200, chapter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ChapterResponseDto>> updateChapterById(String chapterId, ChapterUpdateDto chapterDto) {
        ChapterResponseDto updatedChapter = chapterService.updateChapterById(chapterId, chapterDto);
        //System.out.println("update chapter: "+ updatedChapter);
        ApiResponseDto<ChapterResponseDto> response = new ApiResponseDto<>("Chapter updated successfully", 200, updatedChapter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ChapterResponseDto>> deleteChapterById(String chapterId) {
        ChapterResponseDto deletedChapter = chapterService.deleteChapterById(chapterId);
        ApiResponseDto<ChapterResponseDto> response = new ApiResponseDto<>("Chapter deleted successfully", 200, deletedChapter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<Chapter> searchChapters(String query) {
        return chapterService.searchWithRelevance(query);
    }

    @Override
    public void exportChaptersToCsv(Integer offset, Integer pageSize, HttpServletResponse response) {
        chapterService.exportChaptersToCsv(offset, pageSize, response);

    }
}
