package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterRequestDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.chapter.ChapterUpdateDto;
import com.aspire.asat.cms.model.Chapter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping(value = WebApiUrlConstants.CHAPTER_API, produces = "application/json")
public interface ChapterController {

    @PostMapping
    ResponseEntity<ApiResponseDto<ChapterResponseDto>> createChapter(@RequestBody ChapterRequestDto chapterDto);

    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ChapterResponseDto>>>> getAllChapters(
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "topicId", required = false) String topicId,
                        @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                        @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ChapterResponseWithItemDto>> getChapterById(@PathVariable("id") String chapterId);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ChapterResponseDto>> updateChapterById(@PathVariable("id") String chapterId, @RequestBody ChapterUpdateDto chapterDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ChapterResponseDto>> deleteChapterById(@PathVariable("id") String chapterId);

    @GetMapping(WebApiUrlConstants.PATH_VAR_SEARCH)
    List<Chapter> searchChapters(@RequestParam String query);

    @GetMapping(value = "/export", produces = "text/csv")
    void exportChaptersToCsv(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "1000", required = false) Integer pageSize,
            HttpServletResponse response);

}
