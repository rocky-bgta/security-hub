package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.DeepfakeController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.DeepfakeStep1Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep3Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep5Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep6Request;
import com.aspire.asat.phishing.dto.request.MicroContentCreateRequest;
import com.aspire.asat.phishing.dto.response.BackgroundImageDto;
import com.aspire.asat.phishing.dto.response.BackgroundUploadResponse;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDetailDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStatusResponse;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.dto.response.MicroContentJobResponse;
import com.aspire.asat.phishing.dto.response.MicroContentStatusResponse;
import com.aspire.asat.phishing.dto.response.VideoRenderProviderDto;
import com.aspire.asat.phishing.service.DeepfakeBackgroundService;
import com.aspire.asat.phishing.service.DeepfakeFaceService;
import com.aspire.asat.phishing.service.DeepfakeRenderService;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.service.DeepfakeVoiceService;
import com.aspire.asat.phishing.service.MicroContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeepfakeControllerImpl implements DeepfakeController {

    private final DeepfakeBackgroundService deepfakeBackgroundService;
    private final DeepfakeVideoService deepfakeVideoService;
    private final DeepfakeFaceService deepfakeFaceService;
    private final DeepfakeVoiceService deepfakeVoiceService;
    private final DeepfakeRenderService deepfakeRenderService;
    private final MicroContentService microContentService;

    @Override
    public ResponseEntity<ApiResponseDto<BackgroundUploadResponse>> uploadBackground(MultipartFile backgroundImage) {
        return ok("Background uploaded successfully", deepfakeBackgroundService.uploadBackground(backgroundImage));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<BackgroundImageDto>>>> getBackgroundImages(
            int offset, int pageSize, String fileName, LocalDate uploadDate, Boolean isActive, Boolean isGlobal,
            DeepfakeImageType imageType) {
        Page<BackgroundImageDto> page = deepfakeBackgroundService.listBackgrounds(
                offset, pageSize, fileName, uploadDate, isActive, isGlobal, imageType);
        AllResponseDto<List<BackgroundImageDto>> data = AllResponseDto.<List<BackgroundImageDto>>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .offset(offset)
                .pageSize(pageSize)
                .build();
        return ok("Background images retrieved successfully", data);
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> startStep1(DeepfakeStep1Request request) {
        return ok("Step 1 started successfully", deepfakeVideoService.startStep1(request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep1(UUID videoId, DeepfakeStep1Request request) {
        return ok("Step 1 updated successfully", deepfakeVideoService.updateStep1(videoId, request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep2(
            UUID videoId, MultipartFile faceImage, UUID faceImageId) {
        return ok("Step 2 updated successfully", deepfakeFaceService.updateStep2(videoId, faceImage, faceImageId));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep3(UUID videoId, DeepfakeStep3Request request) {
        return ok("Step 3 updated successfully", deepfakeVideoService.updateStep3(videoId, request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep4(
            UUID videoId, MultipartFile audioSample, String provider, String language,
            UUID voiceCloneId, String[] voiceName, String providerId) {
        return ok("Step 4 updated successfully",
                deepfakeVoiceService.updateStep4(videoId, audioSample, provider, language, voiceCloneId,
                        firstRequestParam(voiceName), providerId));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ClonedVoiceDto>>>> getClonedVoices(
            VoiceCloneProvider provider, LocalDate createdAfter, LocalDate createdBefore, int offset, int pageSize) {
        Page<ClonedVoiceDto> page = deepfakeVoiceService.listClonedVoices(
                provider, createdAfter, createdBefore, offset, pageSize);
        AllResponseDto<List<ClonedVoiceDto>> data = AllResponseDto.<List<ClonedVoiceDto>>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .offset(offset)
                .pageSize(pageSize)
                .build();
        return ok("Cloned voices retrieved successfully", data);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteClonedVoice(UUID voiceCloneId) {
        deepfakeVoiceService.deleteClonedVoice(voiceCloneId);
        return ok("Cloned voice deleted successfully", null);
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep5(UUID videoId, DeepfakeStep5Request request) {
        return ok("Step 5 updated successfully", deepfakeVideoService.updateStep5(videoId, request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<VideoRenderProviderDto>>> getVideoRenderProviders() {
        return ok("Video render providers retrieved successfully",
                deepfakeVideoService.listVideoRenderProviders());
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep6(UUID videoId, DeepfakeStep6Request request) {
        return ok("Video generation started", deepfakeVideoService.updateStep6(videoId, request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoStatusResponse>> getVideoStatus(UUID videoId) {
        return ok("Video status retrieved successfully", deepfakeRenderService.getVideoStatus(videoId));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DeepfakeVideoDto>>>> getDeepfakeVideos(
            int page, int size, LocalDate uploadDate, String title, String description) {
        List<DeepfakeVideoDto> videos = deepfakeVideoService.listVideos(page, size, uploadDate, title, description);
        long total = deepfakeVideoService.countVideos(uploadDate, title, description);
        AllResponseDto<List<DeepfakeVideoDto>> data = AllResponseDto.<List<DeepfakeVideoDto>>builder()
                .items(videos)
                .total(total)
                .offset(page)
                .pageSize(size)
                .build();
        return ok("Deepfake videos retrieved successfully", data);
    }

    @Override
    public ResponseEntity<ApiResponseDto<DeepfakeVideoDetailDto>> getDeepfakeVideo(UUID videoId) {
        return ok("Deepfake video retrieved successfully", deepfakeVideoService.getVideoDetail(videoId));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDeepfakeVideo(UUID videoId) {
        deepfakeVideoService.deleteVideo(videoId);
        return ok("Deepfake video deleted successfully", null);
    }

    @Override
    public ResponseEntity<ApiResponseDto<MicroContentJobResponse>> createMicroContent(MicroContentCreateRequest request) {
        return ok("Micro content generation queued", microContentService.enqueue(request));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> removeMicroContent(UUID videoId) {
        microContentService.removeByVideoId(videoId);
        return ok("Deepfake video removed from micro content successfully", null);
    }

    @Override
    public ResponseEntity<ApiResponseDto<MicroContentStatusResponse>> getMicroContentStatus(UUID jobId) {
        return ok("Micro content status retrieved successfully", microContentService.getStatus(jobId));
    }

    private <T> ResponseEntity<ApiResponseDto<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponseDto.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build());
    }

    /**
     * Spring joins repeated multipart form fields into a String[]; take the first non-blank value.
     */
    private static String firstRequestParam(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
