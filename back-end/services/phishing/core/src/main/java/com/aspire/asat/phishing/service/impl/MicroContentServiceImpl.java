package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsMicroContentClient;
import com.aspire.asat.phishing.constant.MicroContentCmsDefaults;
import com.aspire.asat.phishing.dto.cms.CmsChapterCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsContentCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsTopicCreateRequest;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.request.MicroContentCreateRequest;
import com.aspire.asat.phishing.dto.response.MicroContentJobResponse;
import com.aspire.asat.phishing.dto.response.MicroContentStatusResponse;
import com.aspire.asat.phishing.dto.sqs.MicroContentMessage;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.MicroContentJob;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.MicroContentJobRepository;
import com.aspire.asat.phishing.service.MicroContentService;
import com.aspire.asat.phishing.service.MicroContentSqsService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicroContentServiceImpl implements MicroContentService {

    private final MicroContentJobRepository jobRepository;
    private final DeepfakeRenderJobRepository deepfakeRenderJobRepository;
    private final CmsMicroContentClient cmsMicroContentClient;
    private final MicroContentSqsService microContentSqsService;
    private final UserCurrentContextService userCurrentContextService;
    private final ObjectMapper objectMapper;

    @Value("${deepfake.micro-content.retry.max-attempts:2}")
    private int maxAttempts;

    @Value("${deepfake.micro-content.retry.backoff-ms:1000}")
    private long backoffMs;

    @Value("${deepfake.micro-content.stale-processing-minutes:30}")
    private long staleProcessingMinutes;

    @Override
    public MicroContentJobResponse enqueue(MicroContentCreateRequest request) {
        if (request == null || request.getVideoIds() == null || request.getVideoIds().isEmpty()) {
            throw new ServiceException("Exactly one videoId is required", HttpStatus.BAD_REQUEST);
        }
        // Preserve order, drop duplicate IDs
        List<UUID> videoIds = new ArrayList<>(new LinkedHashSet<>(request.getVideoIds()));
        if (videoIds.size() != 1) {
            throw new ServiceException("Exactly one videoId is required", HttpStatus.BAD_REQUEST);
        }

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String clientId = context.getClientAdminId();
        String clientName = resolveClientName(context, clientId);
        String firstName = MicroContentCmsDefaults.firstNameFrom(clientName);
        UUID jobId = UUID.randomUUID();

        UUID videoId = videoIds.get(0);
        if (jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                clientId,
                videoId.toString(),
                List.of(DeepfakeJobStatus.PENDING, DeepfakeJobStatus.PROCESSING, DeepfakeJobStatus.COMPLETED))) {
            throw new ServiceException(
                    "Deepfake video already uploaded to micro content: " + videoId,
                    HttpStatus.CONFLICT);
        }

        List<MicroContentJob.VideoItem> videos = new ArrayList<>();
        List<MicroContentJob.ItemResult> items = new ArrayList<>();
        MicroContentJob.VideoItem snapshot = snapshotFromDeepfake(videoId, clientId);
        videos.add(snapshot);
        items.add(MicroContentJob.ItemResult.builder()
                .chapterName(snapshot.getChapterName())
                .contentName(snapshot.getContentName())
                .status(DeepfakeJobStatus.PENDING)
                .build());

        String topicName = MicroContentCmsDefaults.resolveTopicName(
                request.getTopicName(), firstName, snapshot.getChapterName());

        MicroContentJob job = MicroContentJob.builder()
                .jobId(jobId)
                .clientId(clientId)
                .clientName(clientName)
                .topicName(topicName)
                .videos(videos)
                .items(items)
                .totalVideos(videos.size())
                .processedVideos(0)
                .status(DeepfakeJobStatus.PENDING)
                .build();
        MicroContentJob saved = jobRepository.save(job);

        microContentSqsService.sendMessage(MicroContentMessage.builder()
                .jobId(saved.getJobId().toString())
                .build());

        log.info("Queued micro content jobId={} clientId={} topicName={} videos={}",
                jobId, clientId, topicName, videos.size());
        return MicroContentJobResponse.builder()
                .jobId(saved.getJobId().toString())
                .status(saved.getStatus())
                .build();
    }

    /**
     * Loads a completed deepfake render job and snapshots fields for CMS topic/chapter/content creation.
     */
    private MicroContentJob.VideoItem snapshotFromDeepfake(UUID videoId, String clientId) {
        DeepfakeRenderJob deepfake = deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Deepfake video not found: " + videoId));

        if (deepfake.getStatus() != DeepfakeJobStatus.COMPLETED) {
            throw new ServiceException(
                    "Deepfake video must be COMPLETED before creating micro content: " + videoId,
                    HttpStatus.CONFLICT);
        }

        String title = trim(deepfake.getTitle());
        if (title == null || title.isBlank()) {
            throw new ServiceException("Deepfake video title is required: " + videoId, HttpStatus.BAD_REQUEST);
        }

        String videoUrl = resolveDurableVideoUrl(deepfake);
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new ServiceException(
                    "Deepfake video has no video artifact (videoS3Key/videoUrl): " + videoId,
                    HttpStatus.BAD_REQUEST);
        }

        String description = deepfake.getDescription() == null ? "" : deepfake.getDescription().trim();

        return MicroContentJob.VideoItem.builder()
                .deepfakeVideoId(videoId.toString())
                .chapterName(title)
                .chapterDescription(description)
                .contentName(title)
                .videoUrl(videoUrl)
                .thumbnailUrl(resolveDurableThumbnailUrl(deepfake))
                .build();
    }

    /**
     * Prefer durable S3 key over ephemeral/external URL for CMS storage.
     */
    private String resolveDurableVideoUrl(DeepfakeRenderJob deepfake) {
        if (deepfake.getVideoS3Key() != null && !deepfake.getVideoS3Key().isBlank()) {
            return deepfake.getVideoS3Key().trim();
        }
        if (deepfake.getVideoUrl() != null && !deepfake.getVideoUrl().isBlank()) {
            return deepfake.getVideoUrl().trim();
        }
        return null;
    }

    private String resolveDurableThumbnailUrl(DeepfakeRenderJob deepfake) {
        if (deepfake.getThumbnailS3Key() != null && !deepfake.getThumbnailS3Key().isBlank()) {
            return deepfake.getThumbnailS3Key().trim();
        }
        return null;
    }

    @Override
    public boolean processJob(Message message) {
        MicroContentMessage payload;
        try {
            payload = objectMapper.readValue(message.body(), MicroContentMessage.class);
        } catch (Exception e) {
            throw new ServiceException("Invalid micro content queue message", HttpStatus.BAD_REQUEST, e);
        }

        UUID jobId = UUID.fromString(payload.getJobId());
        MicroContentJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Micro content job not found: " + jobId));

        if (job.getStatus() == DeepfakeJobStatus.COMPLETED) {
            log.info("microContent jobId={} already completed; skipping duplicate message", jobId);
            return true;
        }
        if (job.getStatus() == DeepfakeJobStatus.PROCESSING && !isStaleProcessing(job)) {
            log.info("microContent jobId={} already processing; deferring duplicate message", jobId);
            return false;
        }
        if (job.getStatus() == DeepfakeJobStatus.PROCESSING) {
            log.warn("microContent jobId={} processing appears stale; retrying", jobId);
        }

        job.setStatus(DeepfakeJobStatus.PROCESSING);
        job.setFailureReason(null);
        jobRepository.save(job);

        Exception lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                runCreationPipeline(job);
                return true;
            } catch (Exception e) {
                lastError = e;
                log.error("microContent jobId={} attempt {}/{} failed", jobId, attempt, maxAttempts, e);
                if (attempt < maxAttempts) {
                    sleep(backoffMs * (1L << (attempt - 1)));
                }
            }
        }

        job.setStatus(DeepfakeJobStatus.FAILED);
        job.setFailureReason(lastError != null ? lastError.getMessage() : "Unknown error");
        jobRepository.save(job);
        throw new ServiceException("microContent jobId=" + jobId + " failed after retries",
                HttpStatus.INTERNAL_SERVER_ERROR, lastError);
    }

    private void runCreationPipeline(MicroContentJob job) {
        List<MicroContentJob.VideoItem> videos = job.getVideos();
        List<MicroContentJob.ItemResult> results = job.getItems();
        int processed = 0;

        for (int i = 0; i < videos.size(); i++) {
            MicroContentJob.VideoItem video = videos.get(i);
            MicroContentJob.ItemResult result = results.get(i);

            if (result.getStatus() == DeepfakeJobStatus.COMPLETED) {
                processed++;
                continue;
            }

            try {
                if (job.getTopicId() == null || job.getTopicId().isBlank()) {
                    String topicId = createTopicWithDuplicateFallback(job, video);
                    job.setTopicId(topicId);
                    job.setTopicCreated(true);
                    jobRepository.save(job);
                }

                String chapterId = result.getChapterId();
                if (chapterId == null || chapterId.isBlank()) {
                    chapterId = cmsMicroContentClient.createChapter(
                            buildChapterRequest(job.getTopicId(), video, 1));
                    result.setChapterId(chapterId);
                    jobRepository.save(job);
                }

                String contentId = cmsMicroContentClient.createContent(buildContentRequest(chapterId, video));
                result.setContentId(contentId);
                result.setStatus(DeepfakeJobStatus.COMPLETED);
                result.setError(null);
                processed++;
                job.setProcessedVideos(processed);
                jobRepository.save(job);
            } catch (RuntimeException e) {
                result.setStatus(DeepfakeJobStatus.FAILED);
                result.setError(e.getMessage());
                job.setProcessedVideos(processed);
                jobRepository.save(job);
                throw e;
            }
        }

        job.setProcessedVideos(processed);
        job.setStatus(DeepfakeJobStatus.COMPLETED);
        job.setFailureReason(null);
        jobRepository.save(job);
        log.info("microContent jobId={} completed with {} content items", job.getJobId(), processed);
    }

    /**
     * Creates the CMS topic using {@code job.topicName}. If CMS reports a duplicate name,
     * retries once with a video-id suffix. Formatted defaults use
     * {@code Micro Content of {firstName}'s {title} ({deepfakeVideoId})}; custom names use
     * {@code {customTopicName} ({deepfakeVideoId})}.
     */
    private String createTopicWithDuplicateFallback(MicroContentJob job, MicroContentJob.VideoItem video) {
        ensureTopicName(job, video);
        try {
            return cmsMicroContentClient.createTopic(buildTopicRequest(job, video));
        } catch (RuntimeException e) {
            if (!isDuplicateTopicNameError(e)) {
                throw e;
            }
            String fallbackName = duplicateFallbackTopicName(job, video);
            log.warn("CMS topic name '{}' already exists; retrying with '{}'",
                    job.getTopicName(), fallbackName);
            job.setTopicName(fallbackName);
            jobRepository.save(job);
            return cmsMicroContentClient.createTopic(buildTopicRequest(job, video));
        }
    }

    private String duplicateFallbackTopicName(MicroContentJob job, MicroContentJob.VideoItem video) {
        String firstName = MicroContentCmsDefaults.firstNameFrom(
                job.getClientName() != null ? job.getClientName() : job.getClientId());
        String defaultName = MicroContentCmsDefaults.topicName(firstName, video.getChapterName());
        if (defaultName.equals(job.getTopicName())) {
            return MicroContentCmsDefaults.topicNameWithVideoId(
                    firstName, video.getChapterName(), video.getDeepfakeVideoId());
        }
        return MicroContentCmsDefaults.withVideoIdSuffix(job.getTopicName(), video.getDeepfakeVideoId());
    }

    /**
     * Ensures {@code job.topicName} is set. Does not overwrite a non-blank custom name already
     * stored on the job (e.g. from enqueue with optional {@code topicName}).
     */
    private void ensureTopicName(MicroContentJob job, MicroContentJob.VideoItem video) {
        if (job.getTopicName() != null && !job.getTopicName().isBlank()) {
            return;
        }
        String clientName = job.getClientName();
        if (clientName == null || clientName.isBlank()) {
            clientName = job.getClientId();
            job.setClientName(clientName);
        }
        job.setTopicName(MicroContentCmsDefaults.topicName(
                MicroContentCmsDefaults.firstNameFrom(clientName), video.getChapterName()));
    }

    /**
     * Prefer clientAdminFullName, then fullName, then clientAdminId.
     */
    private String resolveClientName(CurrentUserContext context, String clientId) {
        String name = firstNonBlank(context.getClientAdminFullName(), context.getFullName(), clientId);
        return name.trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isDuplicateTopicNameError(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.ROOT);
                if (lower.contains("already exists") && lower.contains("topic")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public MicroContentStatusResponse getStatus(UUID jobId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        MicroContentJob job = jobRepository.findByJobIdAndClientId(jobId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Micro content job not found: " + jobId));
        return toStatusResponse(job);
    }

    @Override
    public void removeByVideoId(UUID videoId) {
        if (videoId == null) {
            throw new ServiceException("videoId is required", HttpStatus.BAD_REQUEST);
        }
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        MicroContentJob job = jobRepository.findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                        clientId,
                        videoId.toString(),
                        List.of(DeepfakeJobStatus.PENDING, DeepfakeJobStatus.PROCESSING, DeepfakeJobStatus.COMPLETED))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Micro content not found for deepfake video: " + videoId));

        String topicId = job.getTopicId();
        if (topicId == null || topicId.isBlank()) {
            throw new ResourceNotFoundException(
                    "Micro content topic not yet created for deepfake video: " + videoId);
        }

        cmsMicroContentClient.deleteTopic(topicId);

        job.setStatus(DeepfakeJobStatus.DELETED);
        job.setFailureReason("Removed from micro content");
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
        log.info("Removed micro content for videoId={} clientId={} topicId={} jobId={}",
                videoId, clientId, topicId, job.getJobId());
    }

    private CmsTopicCreateRequest buildTopicRequest(MicroContentJob job, MicroContentJob.VideoItem video) {
        String thumbnail = video.getThumbnailUrl();
        if (thumbnail == null || thumbnail.isBlank()) {
            thumbnail = MicroContentCmsDefaults.TOPIC_THUMBNAIL_URL;
        }
        return CmsTopicCreateRequest.builder()
                .topicName(job.getTopicName())
                .categoryIds(MicroContentCmsDefaults.CATEGORY_IDS)
                .countryIds(MicroContentCmsDefaults.COUNTRY_IDS)
                .complianceIds(MicroContentCmsDefaults.COMPLIANCE_IDS)
                .contentTypeId(MicroContentCmsDefaults.CONTENT_TYPE_ID)
                .durationMinutes(MicroContentCmsDefaults.DURATION_MINUTES)
                .description(MicroContentCmsDefaults.TOPIC_DESCRIPTION)
                .thumbnailUrl(thumbnail)
                .productPackages(MicroContentCmsDefaults.productPackages())
                .tags(new ArrayList<>())
                .status(MicroContentCmsDefaults.TOPIC_STATUS)
                .clientId(job.getClientId())
                .build();
    }

    private CmsChapterCreateRequest buildChapterRequest(String topicId, MicroContentJob.VideoItem video, int position) {
        String description = video.getChapterDescription();
        if (description == null) {
            description = MicroContentCmsDefaults.CHAPTER_DESCRIPTION;
        }
        return CmsChapterCreateRequest.builder()
                .topicId(topicId)
                .chapterName(video.getChapterName())
                .chapterDescription(description)
                .position(position)
                .chapterStatus(MicroContentCmsDefaults.CHAPTER_STATUS)
                .contentIds(new ArrayList<>())
                .build();
    }

    private CmsContentCreateRequest buildContentRequest(String chapterId, MicroContentJob.VideoItem video) {
        return CmsContentCreateRequest.builder()
                .common(CmsContentCreateRequest.Common.builder()
                        .contentName(video.getContentName())
                        .contentType(MicroContentCmsDefaults.CONTENT_TYPE)
                        .status(MicroContentCmsDefaults.CONTENT_STATUS)
                        .chapterIds(List.of(chapterId))
                        .tags(new ArrayList<>())
                        .build())
                .specific(CmsContentCreateRequest.Specific.builder()
                        .updateContent(true)
                        .backgroundFormatting(CmsContentCreateRequest.BackgroundFormatting.builder()
                                .textColor(MicroContentCmsDefaults.BG_TEXT_COLOR)
                                .backgroundColor(MicroContentCmsDefaults.BG_BACKGROUND_COLOR)
                                .backgroundImage(MicroContentCmsDefaults.BG_BACKGROUND_IMAGE)
                                .backgroundOpacity(MicroContentCmsDefaults.BG_BACKGROUND_OPACITY)
                                .tone(MicroContentCmsDefaults.BG_TONE)
                                .build())
                        .captionUrl(MicroContentCmsDefaults.CAPTION_URL)
                        .interactiveVideo(CmsContentCreateRequest.InteractiveVideo.builder()
                                .id(UUID.randomUUID().toString())
                                .videoUrl(video.getVideoUrl())
                                .isProcessing(MicroContentCmsDefaults.INTERACTIVE_VIDEO_PROCESSING)
                                .processingStatus(MicroContentCmsDefaults.INTERACTIVE_VIDEO_PROCESSING_STATUS)
                                .processVideoUrl(video.getVideoUrl())
                                .build())
                        .metadata(CmsContentCreateRequest.Metadata.builder()
                                .additionalProp1(CmsContentCreateRequest.AdditionalProp.builder()
                                        .videoText(MicroContentCmsDefaults.METADATA_VIDEO_TEXT)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private MicroContentStatusResponse toStatusResponse(MicroContentJob job) {
        List<MicroContentStatusResponse.MicroContentItemResult> items = new ArrayList<>();
        if (job.getItems() != null) {
            for (MicroContentJob.ItemResult r : job.getItems()) {
                items.add(MicroContentStatusResponse.MicroContentItemResult.builder()
                        .chapterName(r.getChapterName())
                        .contentName(r.getContentName())
                        .chapterId(r.getChapterId())
                        .contentId(r.getContentId())
                        .status(r.getStatus())
                        .error(r.getError())
                        .build());
            }
        }
        return MicroContentStatusResponse.builder()
                .jobId(job.getJobId().toString())
                .status(job.getStatus())
                .topicId(job.getTopicId())
                .topicCreated(job.isTopicCreated())
                .totalVideos(job.getTotalVideos())
                .processedVideos(job.getProcessedVideos())
                .items(items)
                .failureReason(job.getFailureReason())
                .build();
    }

    private boolean isStaleProcessing(MicroContentJob job) {
        Instant updatedAt = job.getUpdatedAt();
        if (updatedAt == null) {
            return true;
        }
        return updatedAt.isBefore(Instant.now().minus(staleProcessingMinutes, ChronoUnit.MINUTES));
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Retry backoff interrupted", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
