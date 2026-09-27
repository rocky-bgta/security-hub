package com.aspire.asat.phishing.controller;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequestMapping(value = "/api/v1/deepfake", produces = "application/json")
@Tag(name = "Deepfake Video API", description = "6-step wizard for creating deepfake training videos")
public interface DeepfakeController {

    @Operation(summary = "Upload custom background image",
            description = "Uploads a PNG/JPG background used in step 1 when backgroundType is CUSTOM, "
                    + "and persists metadata so it can be reused via the backgrounds list API")
    @PostMapping("/background")
    ResponseEntity<ApiResponseDto<BackgroundUploadResponse>> uploadBackground(@RequestPart("file") MultipartFile backgroundImage);

    @Operation(summary = "List background / face-capture images",
            description = "Returns images visible to the current user (own uploads and/or globals), "
                    + "optionally filtered by imageType (BACKGROUND | FACE_CAPTURE). "
                    + "When imageType is omitted, all types are returned. "
                    + "BACKGROUND includes user backgrounds and global backgrounds; "
                    + "FACE_CAPTURE returns only the current user's face captures. "
                    + "Each item includes imageType and a presigned preview URL.")
    @GetMapping("/backgrounds")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<BackgroundImageDto>>>> getBackgroundImages(
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Filter by file name (case-insensitive contains)")
            @RequestParam(required = false) String fileName,
            @Parameter(description = "Filter by upload date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadDate,
            @Parameter(description = "Filter by active flag") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by global flag (true = globals only, false = user uploads only)")
            @RequestParam(required = false) Boolean isGlobal,
            @Parameter(description = "Filter by image type (BACKGROUND or FACE_CAPTURE). Omit to return all types.")
            @RequestParam(required = false) DeepfakeImageType imageType
    );

    @Operation(summary = "Step 1 - Start wizard", description = "Creates a new deepfake video and saves onboarding fields (title, language, background)")
    @PostMapping("/videos/step/1")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> startStep1(@Valid @RequestBody DeepfakeStep1Request request);

    @Operation(summary = "Step 1 - Update onboarding", description = "Updates video name, language and background on an existing draft or completed video")
    @PutMapping("/videos/{videoId}/step/1")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep1(
            @PathVariable UUID videoId,
            @Valid @RequestBody DeepfakeStep1Request request
    );

    @Operation(summary = "Step 2 - Face capture",
            description = "Either uploads a new face image (framed and saved to the image library for reuse), "
                    + "or reuses a previously uploaded face capture via faceImageId. "
                    + "Provide exactly one of file / faceImageId. "
                    + "List reusable faces via GET /backgrounds?imageType=FACE_CAPTURE.")
    @PutMapping("/videos/{videoId}/step/2")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep2(
            @PathVariable UUID videoId,
            @RequestPart(value = "file", required = false) MultipartFile faceImage,
            @Parameter(description = "Reuse an existing face capture instead of uploading a new image")
            @RequestParam(required = false) UUID faceImageId
    );

    @Operation(summary = "Step 3 - Preview face", description = "Confirms the captured face before voice cloning")
    @PutMapping("/videos/{videoId}/step/3")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep3(
            @PathVariable UUID videoId,
            @Valid @RequestBody DeepfakeStep3Request request
    );

    @Operation(summary = "Step 4 - Voice selection",
            description = "Either uploads an audio sample to create a new one-shot voice clone, or reuses a previously "
                    + "cloned voice via voiceCloneId. Provide exactly one of file / voiceCloneId. "
                    + "voiceName is optional when uploading a new sample. "
                    + "When providerId is provided for a new clone, provider name, credentials, and model come from "
                    + "provider_credentials and the provider query param is ignored (including invalid values); "
                    + "otherwise the provider query param is used.")
    @PutMapping("/videos/{videoId}/step/4")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep4(
            @PathVariable UUID videoId,
            @RequestPart(value = "file", required = false) MultipartFile audioSample,
            @Parameter(description = "Voice cloning provider name (e.g. ELEVENLABS, FISH_AUDIO). "
                    + "Ignored when providerId is set. Default ELEVENLABS.")
            @RequestParam(required = false, defaultValue = "ELEVENLABS") String provider,
            @RequestParam(required = false) String language,
            @Parameter(description = "Reuse an existing cloned voice instead of uploading a new sample")
            @RequestParam(required = false) UUID voiceCloneId,
            @Parameter(description = "Optional display name for the new clone. "
                    + "If the field is sent more than once, only the first value is used.",
                    schema = @Schema(type = "string"))
            @RequestParam(required = false) String[] voiceName,
            @Parameter(description = "Optional provider_credentials document id. When set for a new clone, "
                    + "overrides provider and loads credentials/model from the database.")
            @RequestParam(required = false) String providerId
    );

    @Operation(summary = "List recently cloned voices",
            description = "Returns the current client's previously cloned voices (most recent first) for reuse in Step 4")
    @GetMapping("/voices")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ClonedVoiceDto>>>> getClonedVoices(
            @Parameter(description = "Filter by voice cloning provider") @RequestParam(required = false) VoiceCloneProvider provider,
            @Parameter(description = "Only voices created on/after this date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @Parameter(description = "Only voices created on/before this date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdBefore,
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size (default 10)") @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(summary = "Delete a cloned voice",
            description = "Soft-deletes a previously cloned voice for the current client. "
                    + "Also deletes the voice on the provider (e.g. ElevenLabs) when no other "
                    + "non-deleted clone still references the same provider voice id.")
    @DeleteMapping("/voices/{voiceCloneId}")
    ResponseEntity<ApiResponseDto<Void>> deleteClonedVoice(@PathVariable UUID voiceCloneId);

    @Operation(summary = "Step 5 - Script and speech", description = "Sets the spoken script, synthesizes audio via the voice provider, and stores it for video rendering")
    @PutMapping("/videos/{videoId}/step/5")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep5(
            @PathVariable UUID videoId,
            @Valid @RequestBody DeepfakeStep5Request request
    );

    @Operation(summary = "List video render providers",
            description = "Returns implemented deepfake video render providers for the Step 6 dropdown. "
                    + "Only adapters with a real integration are included (currently HeyGen).")
    @GetMapping("/video-render-providers")
    ResponseEntity<ApiResponseDto<List<VideoRenderProviderDto>>> getVideoRenderProviders();

    @Operation(summary = "Step 6 - Engine selection and render",
            description = "Selects the video render provider and model (via providerId from provider_credentials, "
                    + "or legacy videoProvider + model), then enqueues async rendering")
    @PutMapping("/videos/{videoId}/step/6")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStepResponse>> updateStep6(
            @PathVariable UUID videoId,
            @Valid @RequestBody DeepfakeStep6Request request
    );

    @Operation(summary = "Poll render status", description = "Returns render progress and video URL when completed")
    @GetMapping("/videos/{videoId}/status")
    ResponseEntity<ApiResponseDto<DeepfakeVideoStatusResponse>> getVideoStatus(@PathVariable UUID videoId);

    @Operation(summary = "List deepfake videos",
            description = "Returns a paginated list of deepfake videos with optional filtering")
    @GetMapping("/videos")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DeepfakeVideoDto>>>> getDeepfakeVideos(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by upload date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadDate,
            @Parameter(description = "Filter by title (case-insensitive contains)") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by description (case-insensitive contains)") @RequestParam(required = false) String description
    );

    @Operation(summary = "Get deepfake video details",
            description = "Returns full wizard state for viewing or editing a deepfake video")
    @GetMapping("/videos/{videoId}")
    ResponseEntity<ApiResponseDto<DeepfakeVideoDetailDto>> getDeepfakeVideo(@PathVariable UUID videoId);

    @Operation(summary = "Delete deepfake video",
            description = "Deletes a deepfake video owned by the current client. Deleted videos no longer appear in the list.")
    @DeleteMapping("/videos/{videoId}")
    ResponseEntity<ApiResponseDto<Void>> deleteDeepfakeVideo(@PathVariable UUID videoId);

    @Operation(summary = "Create micro content",
            description = "Queues creation of the per-client 'microContent' topic plus a chapter and VIDEO content "
                    + "per submitted video in CMS. Returns a jobId to poll via the status endpoint.")
    @PostMapping("/micro-content")
    ResponseEntity<ApiResponseDto<MicroContentJobResponse>> createMicroContent(
            @Valid @RequestBody MicroContentCreateRequest request);

    @Operation(summary = "Remove deepfake video from micro content",
            description = "Synchronously removes a deepfake video from micro content by deleting its CMS topic "
                    + "(via CMS deleteTopicById). Does not use SQS. Returns a success message when removed.")
    @DeleteMapping("/micro-content/{videoId}")
    ResponseEntity<ApiResponseDto<Void>> removeMicroContent(@PathVariable UUID videoId);

    @Operation(summary = "Get micro content job status",
            description = "Returns the status and per-video results of an async micro content creation job")
    @GetMapping("/micro-content/{jobId}/status")
    ResponseEntity<ApiResponseDto<MicroContentStatusResponse>> getMicroContentStatus(@PathVariable UUID jobId);
}
