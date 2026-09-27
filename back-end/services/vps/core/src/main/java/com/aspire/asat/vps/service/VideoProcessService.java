package com.aspire.asat.vps.service;

import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface VideoProcessService {

    @Async("vpsTaskExecutor")
    CompletableFuture<ResponseDto> processVideo(RequestDto requestDto) throws Exception;
}
