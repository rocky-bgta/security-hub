package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.data.ContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aspire.asat.registration.constant.WebApiUrlConstants.CONTENT_API;
import static com.aspire.asat.registration.constant.WebApiUrlConstants.CONTENT_COUNT;

@RequestMapping(value = CONTENT_API, produces = "application/json")
public interface ContentController {

    @GetMapping(CONTENT_COUNT)
    ResponseEntity<ApiResponse<List<ContentCountDto>>> getTypeCount();

    @GetMapping
    ResponseEntity<List<ContentDto>> getAllContent(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @PostMapping
    ResponseEntity<ContentDto> createContent(@RequestBody ContentDto saveContentDto);

    @PutMapping
    ResponseEntity<ContentDto> updateContent(@RequestBody ContentDto toBeUpdate);

}