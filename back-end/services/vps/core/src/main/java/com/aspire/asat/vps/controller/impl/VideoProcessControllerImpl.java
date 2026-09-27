package com.aspire.asat.vps.controller.impl;

import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.vps.controller.VideoProcessController;
import com.aspire.asat.vps.dto.response.ApiResponse;
import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;
import com.aspire.asat.vps.service.VideoProcessService;
import com.aspire.asat.vps.utils.VideoProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
public class VideoProcessControllerImpl implements VideoProcessController {

    private final VideoProcessService videoProcessService;
    private final FileProps fileProps;

    @Autowired
    public VideoProcessControllerImpl(VideoProcessService videoProcessService, FileProps fileProps) {
        this.videoProcessService = videoProcessService;
        this.fileProps = fileProps;
    }

    @Override
    public ResponseEntity<ApiResponse<ResponseDto>> processVideo(@RequestBody RequestDto requestDto) throws Exception {
        log.info("[DEPLOY-CHECK] VPS image active — cloudFront.distributionDomain={}",
                fileProps.getAws().getCloudFront().getDistributionDomain());

        ResponseDto initialResponse = VideoProcessUtil.prepareInitialResponse(requestDto);

        CompletableFuture<ResponseDto> responseFuture = videoProcessService.processVideo(requestDto);
        ApiResponse<ResponseDto> apiResponse = new ApiResponse<>("success", 202, initialResponse);
        return ResponseEntity.accepted().body(apiResponse);
    }
}
