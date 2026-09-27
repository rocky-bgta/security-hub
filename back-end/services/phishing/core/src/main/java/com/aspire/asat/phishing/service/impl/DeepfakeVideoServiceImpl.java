package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.enums.UploadToContentStatus;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import com.aspire.asat.phishing.dto.request.DeepfakeStep1Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep3Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep5Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep6Request;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDetailDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.dto.response.VideoRenderProviderDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapterFactory;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.model.MicroContentJob;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.repository.MicroContentJobRepository;
import com.aspire.asat.phishing.service.DeepfakeRenderService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.util.DeepfakeS3KeyUtils;
import com.aspire.asat.phishing.util.ProviderNameNormalizer;
import com.aspire.asat.phishing.util.DeepfakeScriptUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepfakeVideoServiceImpl implements DeepfakeVideoService {

    private final DeepfakeRenderJobRepository renderJobRepository;
    private final DeepfakeVoiceCloneRepository voiceCloneRepository;
    private final DeepfakeS3Service deepfakeS3Service;
    private final VideoRenderAdapterFactory videoRenderAdapterFactory;
    private final VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    private final UserCurrentContextService userCurrentContextService;
    private final DeepfakeRenderService deepfakeRenderService;
    private final ProviderCredentialResolver providerCredentialResolver;
    private final MicroContentJobRepository microContentJobRepository;

    @Override
    public DeepfakeVideoStepResponse startStep1(DeepfakeStep1Request request) {
        validateBackground(request);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        UUID videoId = UUID.randomUUID();

        DeepfakeRenderJob job = DeepfakeRenderJob.builder()
                .renderId(videoId)
                .clientId(clientId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .language(request.getLanguage().trim())
                .backgroundType(request.getBackgroundType())
                .backgroundPreset(request.getBackgroundType() == DeepfakeBackgroundType.PRESET
                        ? request.getBackgroundPreset() : null)
                .backgroundKey(request.getBackgroundType() == DeepfakeBackgroundType.CUSTOM
                        ? normalizeBackgroundKey(request.getBackgroundKey()) : null)
                .status(DeepfakeJobStatus.DRAFT)
                .currentStep(1)
                .build();
        return toStepResponse(renderJobRepository.save(job));
    }

    @Override
    public DeepfakeVideoStepResponse updateStep1(UUID videoId, DeepfakeStep1Request request) {
        DeepfakeRenderJob job = getForEdit(videoId);
        validateBackground(request);

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription());
        job.setLanguage(request.getLanguage().trim());
        job.setBackgroundType(request.getBackgroundType());
        if (request.getBackgroundType() == DeepfakeBackgroundType.PRESET) {
            job.setBackgroundPreset(request.getBackgroundPreset());
            job.setBackgroundKey(null);
        } else {
            job.setBackgroundPreset(null);
            job.setBackgroundKey(normalizeBackgroundKey(request.getBackgroundKey()));
        }
        updateStepProgress(job, 1);
        job.reopenForEdit();
        return toStepResponse(renderJobRepository.save(job));
    }

    @Override
    public DeepfakeVideoStepResponse updateStep3(UUID videoId, DeepfakeStep3Request request) {
        DeepfakeRenderJob job = getForEdit(videoId);
        if (job.getFaceKey() == null || job.getFaceKey().isBlank()) {
            throw new ServiceException("Face capture is required before confirming preview", HttpStatus.CONFLICT);
        }
        if (request.getConfirmed() == null || !request.getConfirmed()) {
            throw new ServiceException("Face confirmation is required to continue", HttpStatus.BAD_REQUEST);
        }
        job.setFaceConfirmed(true);
        updateStepProgress(job, 3);
        job.reopenForEdit();
        return toStepResponse(renderJobRepository.save(job));
    }

    @Override
    public DeepfakeVideoStepResponse updateStep5(UUID videoId, DeepfakeStep5Request request) {
        DeepfakeRenderJob job = getForEdit(videoId);
        if (!job.isFaceConfirmed()) {
            throw new ServiceException("Face must be confirmed before setting script", HttpStatus.CONFLICT);
        }
        if (job.getVoiceCloneId() == null || job.getVoiceCloneId().isBlank()) {
            throw new ServiceException("Voice clone is required before setting script", HttpStatus.CONFLICT);
        }

        DeepfakeVoiceClone voiceClone = voiceCloneRepository.findByVoiceCloneId(UUID.fromString(job.getVoiceCloneId()))
                .filter(clone -> !clone.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Voice clone not found: " + job.getVoiceCloneId()));
        if (voiceClone.getStatus() != DeepfakeJobStatus.COMPLETED || voiceClone.getExternalVoiceId() == null) {
            throw new ServiceException("Voice clone is not ready for speech synthesis", HttpStatus.CONFLICT);
        }

        job.setScript(request.getScript().trim());
        job.setVariables(request.getVariables());
        updateStepProgress(job, 5);

        String resolvedScript = DeepfakeScriptUtils.substituteVariables(job.getScript(), job.getVariables());
        ResolvedProviderCredentials credentials = resolveVoiceCredentials(job.getClientId(), voiceClone);
        byte[] audioBytes = voiceCloneAdapterFactory.getAdapter(voiceClone.getProvider())
                .synthesize(voiceClone.getExternalVoiceId(), resolvedScript, job.getLanguage(), credentials);
        String audioKey = deepfakeS3Service.uploadBytes(audioBytes, "deepfake/audio", "mp3", "audio/mpeg");
        job.setAudioS3Key(audioKey);
        job.setVideoUrl(null);
        job.setVideoS3Key(null);
        job.setThumbnailS3Key(null);
        job.setFailureReason(null);
        job.reopenForEdit();
        return toStepResponse(renderJobRepository.save(job));
    }

    @Override
    public DeepfakeVideoStepResponse updateStep6(UUID videoId, DeepfakeStep6Request request) {
        DeepfakeRenderJob job = getForEdit(videoId);
        if (job.getScript() == null || job.getScript().isBlank()) {
            throw new ServiceException("Script is required before selecting render engine", HttpStatus.CONFLICT);
        }
        if (job.getAudioS3Key() == null || job.getAudioS3Key().isBlank()) {
            throw new ServiceException("Generated audio is required before selecting render engine", HttpStatus.CONFLICT);
        }
        applyVideoProviderSelection(job, request);
        updateStepProgress(job, 6);
        return deepfakeRenderService.enqueueRender(renderJobRepository.save(job));
    }

    private void applyVideoProviderSelection(DeepfakeRenderJob job, DeepfakeStep6Request request) {
        if (StringUtils.hasText(request.getProviderId())) {
            String credentialId = request.getProviderId().trim();
            ResolvedProviderCredentials credentials =
                    providerCredentialResolver.resolveById(job.getClientId(), credentialId);
            if (credentials.getCategory() != ProviderCategory.VIDEO_RENDERING) {
                throw new ServiceException(
                        "Provider credential is not a video rendering provider", HttpStatus.BAD_REQUEST);
            }
            job.setVideoProvider(parseVideoRenderProvider(credentials.getProviderName()));
            job.setModel(credentials.getModelName());
            job.setVideoProviderCredentialId(credentialId);
            return;
        }
        if (request.getVideoProvider() == null) {
            throw new ServiceException(
                    "videoProvider is required when providerId is not provided", HttpStatus.BAD_REQUEST);
        }
        job.setVideoProvider(request.getVideoProvider());
        job.setModel(request.getModel());
        job.setVideoProviderCredentialId(null);
    }

    private ResolvedProviderCredentials resolveVoiceCredentials(String clientId, DeepfakeVoiceClone clone) {
        if (StringUtils.hasText(clone.getProviderCredentialId())) {
            return providerCredentialResolver.resolveById(clientId, clone.getProviderCredentialId());
        }
        return providerCredentialResolver.resolve(clientId, clone.getProvider().name());
    }

    private static String toDisplayName(VideoRenderProvider provider) {
        if (provider == VideoRenderProvider.HEYGEN) {
            return "HeyGen";
        }
        String[] parts = provider.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder displayName = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (displayName.length() > 0) {
                displayName.append(' ');
            }
            displayName.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return displayName.toString();
    }

    private VideoRenderProvider parseVideoRenderProvider(String providerName) {
        if (!StringUtils.hasText(providerName)) {
            throw new ServiceException("Provider credential has no providerName", HttpStatus.BAD_REQUEST);
        }
        VideoRenderProvider parsed = ProviderNameNormalizer.parseEnum(VideoRenderProvider.class, providerName);
        if (parsed == null) {
            throw new ServiceException(
                    "Unsupported video rendering provider: " + providerName.trim(), HttpStatus.BAD_REQUEST);
        }
        return parsed;
    }

    @Override
    public DeepfakeVideoDetailDto getVideoDetail(UUID videoId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeRenderJob job = renderJobRepository.findByRenderIdAndClientId(videoId, clientId)
                .filter(j -> !j.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deepfake video not found: " + videoId));
        return toDetailDto(job);
    }

    @Override
    public List<DeepfakeVideoDto> listVideos(int page, int size, LocalDate uploadDate, String title, String description) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeSize = size > 0 ? size : 10;
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Instant from = uploadDate == null ? null : uploadDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = uploadDate == null ? null : uploadDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<DeepfakeRenderJob> jobs =
                renderJobRepository.findWithFilters(clientId, from, to, title, description, pageable);
        Map<String, UploadToContentStatus> uploadStatuses = resolveUploadStatuses(clientId, jobs);
        return jobs.stream()
                .map(job -> toListDto(job, uploadStatuses.getOrDefault(
                        job.getRenderId().toString(), UploadToContentStatus.PENDING)))
                .toList();
    }

    @Override
    public long countVideos(LocalDate uploadDate, String title, String description) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Instant from = uploadDate == null ? null : uploadDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = uploadDate == null ? null : uploadDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return renderJobRepository.countWithFilters(clientId, from, to, title, description);
    }

    @Override
    public void deleteVideo(UUID videoId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeRenderJob job = renderJobRepository.findByRenderIdAndClientId(videoId, clientId)
                .filter(j -> !j.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deepfake video not found: " + videoId));
        if (job.getStatus() == DeepfakeJobStatus.PENDING || job.getStatus() == DeepfakeJobStatus.PROCESSING) {
            throw new ServiceException("Video cannot be deleted while rendering is in progress", HttpStatus.CONFLICT);
        }
        job.setDeleted(true);
        renderJobRepository.save(job);
        log.info("videoId={} soft-deleted for client={}", videoId, clientId);
    }

    @Override
    public List<VideoRenderProviderDto> listVideoRenderProviders() {
        return videoRenderAdapterFactory.listImplementedProviders().stream()
                .map(provider -> VideoRenderProviderDto.builder()
                        .provider(provider)
                        .displayName(toDisplayName(provider))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DeepfakeRenderJob getForEdit(UUID videoId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeRenderJob job = renderJobRepository.findByRenderIdAndClientId(videoId, clientId)
                .filter(j -> !j.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deepfake video not found: " + videoId));
        if (!job.isEditable()) {
            throw new ServiceException("Video cannot be edited while status is " + job.getStatus(), HttpStatus.CONFLICT);
        }
        return job;
    }

    private void validateBackground(DeepfakeStep1Request request) {
        if (request.getBackgroundType() == DeepfakeBackgroundType.PRESET) {
            if (request.getBackgroundPreset() == null) {
                throw new ServiceException("backgroundPreset is required when backgroundType is PRESET", HttpStatus.BAD_REQUEST);
            }
        } else if (request.getBackgroundType() == DeepfakeBackgroundType.CUSTOM) {
            if (request.getBackgroundKey() == null || request.getBackgroundKey().isBlank()) {
                throw new ServiceException("backgroundKey is required when backgroundType is CUSTOM", HttpStatus.BAD_REQUEST);
            }
            normalizeBackgroundKey(request.getBackgroundKey());
        }
    }

    private String normalizeBackgroundKey(String backgroundKey) {
        String key = DeepfakeS3KeyUtils.normalizeKey(backgroundKey, deepfakeS3Service.getBucket());
        if (key == null || key.isBlank()) {
            throw new ServiceException("backgroundKey is required when backgroundType is CUSTOM", HttpStatus.BAD_REQUEST);
        }
        return key;
    }

    private void updateStepProgress(DeepfakeRenderJob job, int step) {
        job.setCurrentStep(Math.max(job.getCurrentStep(), step));
    }

    private DeepfakeVideoStepResponse toStepResponse(DeepfakeRenderJob job) {
        return DeepfakeVideoStepResponse.builder()
                .videoId(job.getRenderId().toString())
                .currentStep(job.getCurrentStep())
                .status(job.getStatus())
                .build();
    }

    private DeepfakeVideoDto toListDto(DeepfakeRenderJob job, UploadToContentStatus uploadToContent) {
        return DeepfakeVideoDto.builder()
                .id(job.getRenderId().toString())
                .title(job.getTitle())
                .description(job.getDescription())
                .status(job.getStatus())
                .uploadToContent(uploadToContent)
                .videoUrl(resolveVideoUrl(job))
                .thumbnailUrl(resolveThumbnailUrl(job))
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private Map<String, UploadToContentStatus> resolveUploadStatuses(String clientId, List<DeepfakeRenderJob> jobs) {
        if (jobs.isEmpty()) {
            return Map.of();
        }
        Set<String> ids = jobs.stream().map(j -> j.getRenderId().toString()).collect(Collectors.toSet());
        Map<String, UploadToContentStatus> result = new HashMap<>();
        for (MicroContentJob mc : microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(clientId, ids)) {
            if (mc.getVideos() == null) {
                continue;
            }
            UploadToContentStatus mapped = mapUploadStatus(mc.getStatus());
            for (MicroContentJob.VideoItem v : mc.getVideos()) {
                String vid = v.getDeepfakeVideoId();
                if (vid != null && ids.contains(vid)) {
                    result.merge(vid, mapped, DeepfakeVideoServiceImpl::higherPrecedence);
                }
            }
        }
        return result;
    }

    private static UploadToContentStatus mapUploadStatus(DeepfakeJobStatus status) {
        if (status == DeepfakeJobStatus.COMPLETED) {
            return UploadToContentStatus.DONE;
        }
        if (status == DeepfakeJobStatus.PENDING || status == DeepfakeJobStatus.PROCESSING) {
            return UploadToContentStatus.PROCESSING;
        }
        // DELETED / FAILED / other → PENDING so the same video can be uploaded again
        return UploadToContentStatus.PENDING;
    }

    private static UploadToContentStatus higherPrecedence(UploadToContentStatus a, UploadToContentStatus b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    private DeepfakeVideoDetailDto toDetailDto(DeepfakeRenderJob job) {
        String facePreviewUrl = presignSafe(job.getFaceKey());
        String backgroundPreviewUrl = presignSafe(job.getBackgroundKey());
        String audioPreviewUrl = presignSafe(job.getAudioS3Key());

        return DeepfakeVideoDetailDto.builder()
                .id(job.getRenderId().toString())
                .title(job.getTitle())
                .description(job.getDescription())
                .currentStep(job.getCurrentStep())
                .faceKey(job.getFaceKey())
                .facePreviewUrl(facePreviewUrl)
                .faceConfirmed(job.isFaceConfirmed())
                .voiceCloneId(job.getVoiceCloneId())
                .script(job.getScript())
                .language(job.getLanguage())
                .backgroundType(job.getBackgroundType())
                .backgroundPreset(job.getBackgroundPreset())
                .backgroundKey(job.getBackgroundKey())
                .backgroundPreviewUrl(backgroundPreviewUrl)
                .audioPreviewUrl(audioPreviewUrl)
                .videoProvider(job.getVideoProvider())
                .model(job.getModel())
                .variables(job.getVariables())
                .status(job.getStatus())
                .videoUrl(resolveVideoUrl(job))
                .thumbnailUrl(resolveThumbnailUrl(job))
                .failureReason(job.getFailureReason())
                .uploadDate(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private String resolveThumbnailUrl(DeepfakeRenderJob job) {
        if (job.getStatus() != DeepfakeJobStatus.COMPLETED) {
            return null;
        }
        return presignSafe(job.getThumbnailS3Key());
    }

    private String resolveVideoUrl(DeepfakeRenderJob job) {
        if (job.getStatus() != DeepfakeJobStatus.COMPLETED) {
            return null;
        }
        if (job.getVideoS3Key() != null && !job.getVideoS3Key().isBlank()) {
            return presignSafe(job.getVideoS3Key());
        }
        return job.getVideoUrl();
    }

    private String presignSafe(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }
        try {
            return deepfakeS3Service.presignGetUrl(s3Key);
        } catch (Exception e) {
            log.warn("Failed to presign url for key {}", s3Key, e);
            return null;
        }
    }
}
