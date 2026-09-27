package com.aspire.asat.vps.controller;

import com.aspire.asat.vps.constant.WebApiUrlConstants;
import com.aspire.asat.vps.dto.response.ApiResponse;
import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@RequestMapping(value = WebApiUrlConstants.VIDEO_PROCESS_API, produces = "application/json")
public interface VideoProcessController {
    @PostMapping
    ResponseEntity<ApiResponse<ResponseDto>> processVideo(@RequestBody RequestDto requestDto) throws Exception;
}
