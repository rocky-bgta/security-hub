package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsMicroContentClient;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.services.sqs.model.Message;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MicroContentServiceImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CLIENT_ADMIN_FULL_NAME = "Acme Admin";
    private static final String ADMIN_FIRST_NAME = "Acme";

    @Mock
    private MicroContentJobRepository jobRepository;
    @Mock
    private DeepfakeRenderJobRepository deepfakeRenderJobRepository;
    @Mock
    private CmsMicroContentClient cmsMicroContentClient;
    @Mock
    private com.aspire.asat.phishing.service.MicroContentSqsService microContentSqsService;
    @Mock
    private com.aspire.asat.phishing.utils.UserCurrentContextService userCurrentContextService;
    @Mock
    private CurrentUserContext currentUserContext;
    @Mock
    private ObjectMapper objectMapper;

    private MicroContentServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new MicroContentServiceImpl(
                jobRepository,
                deepfakeRenderJobRepository,
                cmsMicroContentClient,
                microContentSqsService,
                userCurrentContextService,
                objectMapper);
        setField(service, "maxAttempts", 2);
        setField(service, "backoffMs", 1L);
        setField(service, "staleProcessingMinutes", 30L);
    }

    // ---------------------------------------------------------------- enqueue

    @Test
    void enqueue_LoadsDeepfakeJobsAndSnapshotsFields() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(
                        videoId, " One ", " Desc One ", "deepfake/video/one.mp4", null, "deepfake/thumbnail/one.jpg")));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MicroContentJobResponse response = service.enqueue(requestWith(videoId));

        ArgumentCaptor<MicroContentJob> jobCaptor = ArgumentCaptor.forClass(MicroContentJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        MicroContentJob savedJob = jobCaptor.getValue();

        assertEquals(DeepfakeJobStatus.PENDING, savedJob.getStatus());
        assertEquals(CLIENT_ID, savedJob.getClientId());
        assertEquals(CLIENT_ADMIN_FULL_NAME, savedJob.getClientName());
        assertEquals(expectedTopicName("One"), savedJob.getTopicName());
        assertEquals(1, savedJob.getTotalVideos());
        assertEquals(0, savedJob.getProcessedVideos());

        MicroContentJob.VideoItem first = savedJob.getVideos().get(0);
        assertEquals(videoId.toString(), first.getDeepfakeVideoId());
        assertEquals("One", first.getChapterName());
        assertEquals("One", first.getContentName());
        assertEquals("Desc One", first.getChapterDescription());
        assertEquals("deepfake/video/one.mp4", first.getVideoUrl());
        assertEquals("deepfake/thumbnail/one.jpg", first.getThumbnailUrl());

        ArgumentCaptor<MicroContentMessage> msgCaptor = ArgumentCaptor.forClass(MicroContentMessage.class);
        verify(microContentSqsService).sendMessage(msgCaptor.capture());
        assertEquals(savedJob.getJobId().toString(), msgCaptor.getValue().getJobId());
        assertEquals(savedJob.getJobId().toString(), response.getJobId());
        assertEquals(DeepfakeJobStatus.PENDING, response.getStatus());
    }

    @Test
    void enqueue_FallsBackToClientId_WhenFullNamesBlank() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(currentUserContext);
        when(currentUserContext.getClientAdminId()).thenReturn(CLIENT_ID);
        when(currentUserContext.getClientAdminFullName()).thenReturn("  ");
        when(currentUserContext.getFullName()).thenReturn(null);
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(videoId, "Title", "Desc", "key.mp4", null, null)));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.enqueue(requestWith(videoId));

        ArgumentCaptor<MicroContentJob> jobCaptor = ArgumentCaptor.forClass(MicroContentJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertEquals(CLIENT_ID, jobCaptor.getValue().getClientName());
        assertEquals("Micro Content of " + CLIENT_ID + "'s Title", jobCaptor.getValue().getTopicName());
    }

    @Test
    void enqueue_WithCustomTopicName_UsesProvidedName() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(videoId, "Video Title", "Desc", "key.mp4", null, null)));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MicroContentCreateRequest request = MicroContentCreateRequest.builder()
                .videoIds(List.of(videoId))
                .topicName("  My Custom Topic  ")
                .build();
        service.enqueue(request);

        ArgumentCaptor<MicroContentJob> jobCaptor = ArgumentCaptor.forClass(MicroContentJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertEquals("My Custom Topic", jobCaptor.getValue().getTopicName());
    }

    @Test
    void enqueue_WithBlankTopicName_FallsBackToFormattedDefault() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(videoId, "Alert", "Desc", "key.mp4", null, null)));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MicroContentCreateRequest request = MicroContentCreateRequest.builder()
                .videoIds(List.of(videoId))
                .topicName("   ")
                .build();
        service.enqueue(request);

        ArgumentCaptor<MicroContentJob> jobCaptor = ArgumentCaptor.forClass(MicroContentJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertEquals(expectedTopicName("Alert"), jobCaptor.getValue().getTopicName());
    }

    @Test
    void enqueue_NoVideoIds_ThrowsBadRequest() {
        MicroContentCreateRequest request = MicroContentCreateRequest.builder()
                .videoIds(new ArrayList<>())
                .build();
        ServiceException ex = assertThrows(ServiceException.class, () -> service.enqueue(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_MoreThanOneVideoId_ThrowsBadRequest() {
        UUID videoId1 = UUID.randomUUID();
        UUID videoId2 = UUID.randomUUID();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.enqueue(requestWith(videoId1, videoId2)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_AlreadyUploadedDeepfake_ThrowsConflict() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(true);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.enqueue(requestWith(videoId)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(deepfakeRenderJobRepository, never()).findByRenderIdAndClientId(any(), any());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_DeepfakeNotFound_Throws() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.enqueue(requestWith(videoId)));
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_DeepfakeNotCompleted_Throws() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        DeepfakeRenderJob draft = completedDeepfake(videoId, "Title", "Desc", "key.mp4", null, null);
        draft.setStatus(DeepfakeJobStatus.DRAFT);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(draft));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.enqueue(requestWith(videoId)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_MissingVideoArtifact_Throws() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(videoId, "Title", "Desc", null, null, null)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.enqueue(requestWith(videoId)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    @Test
    void enqueue_BlankTitle_Throws() {
        stubClientContext();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(false);
        when(deepfakeRenderJobRepository.findByRenderIdAndClientId(videoId, CLIENT_ID))
                .thenReturn(Optional.of(completedDeepfake(videoId, "  ", "Desc", "key.mp4", null, null)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.enqueue(requestWith(videoId)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(microContentSqsService, never()).sendMessage(any());
    }

    // -------------------------------------------------------------- processJob

    @Test
    void processJob_CreatesTopicChapterAndContent_WithVideoThumbnail() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc One", "url-1", "thumb-1.jpg"));
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createTopic(any())).thenReturn("topic-1");
        when(cmsMicroContentClient.createChapter(any())).thenReturn("chapter-1");
        when(cmsMicroContentClient.createContent(any())).thenReturn("content-1");

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);

        ArgumentCaptor<CmsTopicCreateRequest> topicCaptor = ArgumentCaptor.forClass(CmsTopicCreateRequest.class);
        verify(cmsMicroContentClient, times(1)).createTopic(topicCaptor.capture());
        assertEquals(expectedTopicName("One"), topicCaptor.getValue().getTopicName());
        assertEquals(CLIENT_ID, topicCaptor.getValue().getClientId());
        assertEquals("ENABLED", topicCaptor.getValue().getStatus());
        assertEquals("thumb-1.jpg", topicCaptor.getValue().getThumbnailUrl());

        ArgumentCaptor<CmsChapterCreateRequest> chapterCaptor = ArgumentCaptor.forClass(CmsChapterCreateRequest.class);
        verify(cmsMicroContentClient).createChapter(chapterCaptor.capture());
        assertEquals("topic-1", chapterCaptor.getValue().getTopicId());
        assertEquals(1, chapterCaptor.getValue().getPosition());
        assertEquals("One", chapterCaptor.getValue().getChapterName());
        assertEquals("Desc One", chapterCaptor.getValue().getChapterDescription());

        ArgumentCaptor<CmsContentCreateRequest> contentCaptor = ArgumentCaptor.forClass(CmsContentCreateRequest.class);
        verify(cmsMicroContentClient).createContent(contentCaptor.capture());
        assertEquals(List.of("chapter-1"), contentCaptor.getValue().getCommon().getChapterIds());
        assertEquals("One", contentCaptor.getValue().getCommon().getContentName());
        assertEquals("url-1", contentCaptor.getValue().getSpecific().getInteractiveVideo().getVideoUrl());
        assertEquals("url-1", contentCaptor.getValue().getSpecific().getInteractiveVideo().getProcessVideoUrl());
        assertFalse(contentCaptor.getValue().getSpecific().getInteractiveVideo().isProcessing());
        assertEquals("PROCESSED", contentCaptor.getValue().getSpecific().getInteractiveVideo().getProcessingStatus());
        assertEquals("VIDEO", contentCaptor.getValue().getCommon().getContentType());

        assertEquals(DeepfakeJobStatus.COMPLETED, job.getStatus());
        assertTrue(job.isTopicCreated());
        assertEquals("topic-1", job.getTopicId());
        assertEquals(1, job.getProcessedVideos());
    }

    @Test
    void processJob_DuplicateTopicName_RetriesWithVideoIdSuffix() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", null));
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createTopic(any()))
                .thenThrow(new ServiceException(
                        "Failed to create CMS topic: Topic with name '" + expectedTopicName("One") + "' already exists.",
                        HttpStatus.BAD_GATEWAY))
                .thenReturn("topic-fallback");
        when(cmsMicroContentClient.createChapter(any())).thenReturn("chapter-1");
        when(cmsMicroContentClient.createContent(any())).thenReturn("content-1");

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);
        ArgumentCaptor<CmsTopicCreateRequest> topicCaptor = ArgumentCaptor.forClass(CmsTopicCreateRequest.class);
        verify(cmsMicroContentClient, times(2)).createTopic(topicCaptor.capture());
        List<CmsTopicCreateRequest> topicRequests = topicCaptor.getAllValues();
        assertEquals(expectedTopicName("One"), topicRequests.get(0).getTopicName());
        assertEquals(expectedTopicName("One") + " (vid-1)", topicRequests.get(1).getTopicName());
        assertEquals("", topicRequests.get(0).getThumbnailUrl());
        assertEquals("topic-fallback", job.getTopicId());
        assertEquals(DeepfakeJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void processJob_DuplicateCustomTopicName_RetriesWithCustomNamePlusVideoId() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", null));
        job.setTopicName("My Custom Topic");
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createTopic(any()))
                .thenThrow(new ServiceException(
                        "Failed to create CMS topic: Topic with name 'My Custom Topic' already exists.",
                        HttpStatus.BAD_GATEWAY))
                .thenReturn("topic-fallback");
        when(cmsMicroContentClient.createChapter(any())).thenReturn("chapter-1");
        when(cmsMicroContentClient.createContent(any())).thenReturn("content-1");

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);
        ArgumentCaptor<CmsTopicCreateRequest> topicCaptor = ArgumentCaptor.forClass(CmsTopicCreateRequest.class);
        verify(cmsMicroContentClient, times(2)).createTopic(topicCaptor.capture());
        List<CmsTopicCreateRequest> topicRequests = topicCaptor.getAllValues();
        assertEquals("My Custom Topic", topicRequests.get(0).getTopicName());
        assertEquals("My Custom Topic (vid-1)", topicRequests.get(1).getTopicName());
        assertEquals("My Custom Topic (vid-1)", job.getTopicName());
        assertEquals("topic-fallback", job.getTopicId());
        assertEquals(DeepfakeJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void processJob_ResumesWhenTopicAlreadyCreated_DoesNotRecreateTopic() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", "thumb.jpg"));
        job.setTopicId("topic-existing");
        job.setTopicCreated(true);
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createChapter(any())).thenReturn("chapter-1");
        when(cmsMicroContentClient.createContent(any())).thenReturn("content-1");

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);
        verify(cmsMicroContentClient, never()).createTopic(any());
        ArgumentCaptor<CmsChapterCreateRequest> chapterCaptor = ArgumentCaptor.forClass(CmsChapterCreateRequest.class);
        verify(cmsMicroContentClient).createChapter(chapterCaptor.capture());
        assertEquals("topic-existing", chapterCaptor.getValue().getTopicId());
        assertEquals(1, chapterCaptor.getValue().getPosition());
        verify(cmsMicroContentClient).createContent(any());
        assertEquals(DeepfakeJobStatus.COMPLETED, job.getStatus());
        assertEquals(1, job.getProcessedVideos());
    }

    @Test
    void processJob_ResumesWhenChapterAlreadyCreated_DoesNotRecreateChapter() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", "thumb.jpg"));
        job.setTopicId("topic-1");
        job.setTopicCreated(true);
        job.getItems().get(0).setChapterId("chapter-existing");
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createContent(any())).thenReturn("content-1");

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);
        verify(cmsMicroContentClient, never()).createTopic(any());
        verify(cmsMicroContentClient, never()).createChapter(any());
        ArgumentCaptor<CmsContentCreateRequest> contentCaptor = ArgumentCaptor.forClass(CmsContentCreateRequest.class);
        verify(cmsMicroContentClient).createContent(contentCaptor.capture());
        assertEquals(List.of("chapter-existing"), contentCaptor.getValue().getCommon().getChapterIds());
        assertEquals(DeepfakeJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void processJob_AlreadyCompleted_SkipsAndAcks() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", null));
        job.setStatus(DeepfakeJobStatus.COMPLETED);
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));

        boolean result = service.processJob(message(job.getJobId()));

        assertTrue(result);
        verify(cmsMicroContentClient, never()).createTopic(any());
        verify(cmsMicroContentClient, never()).createChapter(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void processJob_AlreadyProcessingFresh_Defers() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", null));
        job.setStatus(DeepfakeJobStatus.PROCESSING);
        job.setUpdatedAt(Instant.now());
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));

        boolean result = service.processJob(message(job.getJobId()));

        assertFalse(result);
        verify(cmsMicroContentClient, never()).createChapter(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void processJob_JobNotFound_Throws() throws Exception {
        UUID jobId = UUID.randomUUID();
        stubReadValue(jobId);
        when(jobRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.processJob(message(jobId)));
    }

    @Test
    void processJob_CmsFailure_MarksFailedAndThrows() throws Exception {
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", "thumb.jpg"));
        stubReadValue(job.getJobId());
        when(jobRepository.findByJobId(job.getJobId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cmsMicroContentClient.createTopic(any())).thenReturn("topic-1");
        when(cmsMicroContentClient.createChapter(any())).thenThrow(new RuntimeException("boom"));

        Message message = message(job.getJobId());
        assertThrows(ServiceException.class, () -> service.processJob(message));

        assertEquals(DeepfakeJobStatus.FAILED, job.getStatus());
        assertEquals("boom", job.getFailureReason());
        // topic created once, then chapter retried maxAttempts times
        verify(cmsMicroContentClient, times(1)).createTopic(any());
        verify(cmsMicroContentClient, times(2)).createChapter(any());
        verify(cmsMicroContentClient, never()).createContent(any());
    }

    @Test
    void processJob_InvalidMessageBody_Throws() throws Exception {
        when(objectMapper.readValue(anyString(), eq(MicroContentMessage.class)))
                .thenThrow(new RuntimeException("bad json"));
        Message message = Message.builder().body("not-json").build();
        assertThrows(ServiceException.class, () -> service.processJob(message));
    }

    // --------------------------------------------------------------- getStatus

    @Test
    void getStatus_ReturnsMappedResponse() {
        stubClientContextForStatus();
        MicroContentJob job = pendingJob(snapshot("vid-1", "One", "Desc", "url-1", null));
        job.setStatus(DeepfakeJobStatus.COMPLETED);
        job.setTopicId("topic-1");
        job.setTopicCreated(true);
        job.setProcessedVideos(1);
        job.getItems().get(0).setChapterId("chapter-1");
        job.getItems().get(0).setContentId("content-1");
        job.getItems().get(0).setStatus(DeepfakeJobStatus.COMPLETED);

        when(jobRepository.findByJobIdAndClientId(job.getJobId(), CLIENT_ID)).thenReturn(Optional.of(job));

        MicroContentStatusResponse response = service.getStatus(job.getJobId());

        assertEquals(job.getJobId().toString(), response.getJobId());
        assertEquals(DeepfakeJobStatus.COMPLETED, response.getStatus());
        assertEquals("topic-1", response.getTopicId());
        assertTrue(response.isTopicCreated());
        assertEquals(1, response.getTotalVideos());
        assertEquals(1, response.getProcessedVideos());
        assertEquals("chapter-1", response.getItems().get(0).getChapterId());
        assertEquals("content-1", response.getItems().get(0).getContentId());
        assertNull(response.getFailureReason());
    }

    @Test
    void getStatus_NotFound_Throws() {
        stubClientContextForStatus();
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findByJobIdAndClientId(jobId, CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getStatus(jobId));
    }

    // ---------------------------------------------------------------- removeByVideoId

    @Test
    void removeByVideoId_DeletesCmsTopicAndMarksJobDeleted() {
        stubClientContextForStatus();
        UUID videoId = UUID.randomUUID();
        MicroContentJob job = pendingJob(snapshot(videoId.toString(), "One", "Desc", "key.mp4", null));
        job.setStatus(DeepfakeJobStatus.COMPLETED);
        job.setTopicId("topic-1");
        job.setTopicCreated(true);
        when(jobRepository.findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.removeByVideoId(videoId);

        verify(cmsMicroContentClient).deleteTopic("topic-1");
        ArgumentCaptor<MicroContentJob> jobCaptor = ArgumentCaptor.forClass(MicroContentJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertEquals(DeepfakeJobStatus.DELETED, jobCaptor.getValue().getStatus());
        assertEquals("Removed from micro content", jobCaptor.getValue().getFailureReason());
    }

    @Test
    void removeByVideoId_NotFound_Throws() {
        stubClientContextForStatus();
        UUID videoId = UUID.randomUUID();
        when(jobRepository.findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.removeByVideoId(videoId));
        verify(cmsMicroContentClient, never()).deleteTopic(anyString());
    }

    @Test
    void removeByVideoId_MissingTopicId_Throws() {
        stubClientContextForStatus();
        UUID videoId = UUID.randomUUID();
        MicroContentJob job = pendingJob(snapshot(videoId.toString(), "One", "Desc", "key.mp4", null));
        job.setTopicId(null);
        when(jobRepository.findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(Optional.of(job));

        assertThrows(ResourceNotFoundException.class, () -> service.removeByVideoId(videoId));
        verify(cmsMicroContentClient, never()).deleteTopic(anyString());
    }

    @Test
    void removeByVideoId_NullVideoId_ThrowsBadRequest() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.removeByVideoId(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(cmsMicroContentClient, never()).deleteTopic(anyString());
    }

    @Test
    void removeByVideoId_CmsFailure_PropagatesAndDoesNotMarkDeleted() {
        stubClientContextForStatus();
        UUID videoId = UUID.randomUUID();
        MicroContentJob job = pendingJob(snapshot(videoId.toString(), "One", "Desc", "key.mp4", null));
        job.setStatus(DeepfakeJobStatus.COMPLETED);
        job.setTopicId("topic-1");
        when(jobRepository.findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
                eq(CLIENT_ID), eq(videoId.toString()), any()))
                .thenReturn(Optional.of(job));
        org.mockito.Mockito.doThrow(new ServiceException("Failed to delete CMS topic", HttpStatus.BAD_GATEWAY))
                .when(cmsMicroContentClient).deleteTopic("topic-1");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.removeByVideoId(videoId));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
        verify(jobRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- helpers

    private static String expectedTopicName(String videoTitle) {
        return "Micro Content of " + ADMIN_FIRST_NAME + "'s " + videoTitle;
    }

    private void stubClientContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(currentUserContext);
        when(currentUserContext.getClientAdminId()).thenReturn(CLIENT_ID);
        when(currentUserContext.getClientAdminFullName()).thenReturn(CLIENT_ADMIN_FULL_NAME);
    }

    private void stubClientContextForStatus() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(currentUserContext);
        when(currentUserContext.getClientAdminId()).thenReturn(CLIENT_ID);
    }

    private MicroContentCreateRequest requestWith(UUID... videoIds) {
        return MicroContentCreateRequest.builder()
                .videoIds(new ArrayList<>(List.of(videoIds)))
                .build();
    }

    private DeepfakeRenderJob completedDeepfake(
            UUID renderId, String title, String description, String videoS3Key, String videoUrl, String thumbnailS3Key) {
        return DeepfakeRenderJob.builder()
                .renderId(renderId)
                .clientId(CLIENT_ID)
                .title(title)
                .description(description)
                .videoS3Key(videoS3Key)
                .videoUrl(videoUrl)
                .thumbnailS3Key(thumbnailS3Key)
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
    }

    private MicroContentJob.VideoItem snapshot(
            String deepfakeVideoId, String title, String description, String videoUrl, String thumbnailUrl) {
        return MicroContentJob.VideoItem.builder()
                .deepfakeVideoId(deepfakeVideoId)
                .chapterName(title)
                .chapterDescription(description)
                .contentName(title)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    private MicroContentJob pendingJob(MicroContentJob.VideoItem... items) {
        List<MicroContentJob.VideoItem> videos = new ArrayList<>(List.of(items));
        List<MicroContentJob.ItemResult> results = new ArrayList<>();
        for (MicroContentJob.VideoItem item : items) {
            results.add(MicroContentJob.ItemResult.builder()
                    .chapterName(item.getChapterName())
                    .contentName(item.getContentName())
                    .status(DeepfakeJobStatus.PENDING)
                    .build());
        }
        String title = videos.isEmpty() ? "Video" : videos.get(0).getChapterName();
        return MicroContentJob.builder()
                .jobId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .clientName(CLIENT_ADMIN_FULL_NAME)
                .topicName(expectedTopicName(title))
                .videos(videos)
                .items(results)
                .totalVideos(videos.size())
                .processedVideos(0)
                .status(DeepfakeJobStatus.PENDING)
                .build();
    }

    private Message message(UUID jobId) {
        return Message.builder().body("{\"jobId\":\"" + jobId + "\"}").build();
    }

    private void stubReadValue(UUID jobId) throws Exception {
        when(objectMapper.readValue(anyString(), eq(MicroContentMessage.class)))
                .thenReturn(MicroContentMessage.builder().jobId(jobId.toString()).build());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
