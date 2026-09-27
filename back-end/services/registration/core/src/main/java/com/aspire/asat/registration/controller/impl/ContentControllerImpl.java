package com.aspire.asat.registration.controller.impl;


import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.data.ContentDto;
import com.aspire.asat.registration.controller.ContentController;
import com.aspire.asat.registration.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class ContentControllerImpl implements ContentController {

    private final ContentService contentService;

    public ContentControllerImpl(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public ResponseEntity<ContentDto> createContent(@RequestBody ContentDto saveContentDto) {
        ContentDto savedContent = contentService.save(saveContentDto);
        return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponse<List<ContentCountDto>>> getTypeCount() {
        ApiResponse<List<ContentCountDto>> response = contentService.getTypeCount();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ContentDto>> getAllContent(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize) {
        List<ContentDto> content = contentService.getAllContent(offset, pageSize);
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContentDto> updateContent(@RequestBody ContentDto toBeUpdate) {
        ContentDto contentDtoFromDb = contentService.updateContent(toBeUpdate);
        return new ResponseEntity<>(contentDtoFromDb, HttpStatus.OK);
    }

}