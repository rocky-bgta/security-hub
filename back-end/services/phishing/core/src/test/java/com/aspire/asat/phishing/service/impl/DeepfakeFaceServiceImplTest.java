package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.service.DeepfakeFaceFramingService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeFaceServiceImplTest {

    private static final String CLIENT_ID = "client-admin-1";

    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private DeepfakeVideoService deepfakeVideoService;
    @Mock
    private DeepfakeRenderJobRepository renderJobRepository;
    @Mock
    private DeepfakeFaceFramingService faceFramingService;
    @Mock
    private DeepfakeBackgroundImageRepository backgroundImageRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private DeepfakeFaceServiceImpl service;

    private static byte[] png() throws Exception {
        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static DeepfakeRenderJob step1CompleteJob() {
        return DeepfakeRenderJob.builder()
                .renderId(UUID.randomUUID())
                .clientId("client-1")
                .title("Video")
                .language("en")
                .backgroundType(DeepfakeBackgroundType.PRESET)
                .backgroundPreset(com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset.OFFICE)
                .status(DeepfakeJobStatus.DRAFT)
                .currentStep(1)
                .build();
    }

    private void stubClient() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    @Test
    void updateStep2_framesFace_persistsLibraryRow_andStoresResult() throws Exception {
        UUID videoId = UUID.randomUUID();
        byte[] pngBytes = png();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) pngBytes.length);
        when(file.getOriginalFilename()).thenReturn("face.png");
        when(file.getBytes()).thenReturn(pngBytes);
        DeepfakeRenderJob job = step1CompleteJob();
        byte[] framed = new byte[]{1, 2, 3};

        stubClient();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        when(faceFramingService.frameToLandscape(any())).thenReturn(framed);
        when(deepfakeS3Service.uploadBytes(eq(framed), eq("deepfake/face"), eq("jpg"), eq("image/jpeg")))
                .thenReturn("deepfake/face/abc.jpg");
        when(backgroundImageRepository.save(any(DeepfakeBackgroundImage.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));

        DeepfakeVideoStepResponse response = service.updateStep2(videoId, file, null);

        assertEquals(2, response.getCurrentStep());
        verify(faceFramingService).frameToLandscape(any());
        verify(deepfakeS3Service).uploadBytes(eq(framed), eq("deepfake/face"), eq("jpg"), eq("image/jpeg"));
        assertEquals("deepfake/face/abc.jpg", job.getFaceKey());
        assertFalse(job.isFaceConfirmed());

        ArgumentCaptor<DeepfakeBackgroundImage> captor = ArgumentCaptor.forClass(DeepfakeBackgroundImage.class);
        verify(backgroundImageRepository).save(captor.capture());
        DeepfakeBackgroundImage saved = captor.getValue();
        assertEquals(DeepfakeImageType.FACE_CAPTURE, saved.getImageType());
        assertEquals(CLIENT_ID, saved.getClientAdminId());
        assertEquals("deepfake/face/abc.jpg", saved.getFileKey());
        assertEquals("face.png", saved.getFileName());
        assertFalse(saved.isGlobal());
        assertTrue(saved.isActive());
        assertEquals(BackgroundImageStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void updateStep2_reusesExistingFaceImageId() {
        UUID videoId = UUID.randomUUID();
        UUID faceImageId = UUID.randomUUID();
        DeepfakeRenderJob job = step1CompleteJob();
        DeepfakeBackgroundImage existing = DeepfakeBackgroundImage.builder()
                .backgroundImageId(faceImageId)
                .fileName("face.png")
                .fileKey("deepfake/face/existing.jpg")
                .clientAdminId(CLIENT_ID)
                .imageType(DeepfakeImageType.FACE_CAPTURE)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .build();

        stubClient();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        when(backgroundImageRepository.findByBackgroundImageId(faceImageId)).thenReturn(Optional.of(existing));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));

        DeepfakeVideoStepResponse response = service.updateStep2(videoId, null, faceImageId);

        assertEquals(2, response.getCurrentStep());
        assertEquals("deepfake/face/existing.jpg", job.getFaceKey());
        verify(faceFramingService, never()).frameToLandscape(any());
        verify(deepfakeS3Service, never()).uploadBytes(any(), any(), any(), any());
        verify(backgroundImageRepository, never()).save(any());
    }

    @Test
    void updateStep2_rejectsBothFileAndFaceImageId() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        assertThrows(ServiceException.class,
                () -> service.updateStep2(UUID.randomUUID(), file, UUID.randomUUID()));
    }

    @Test
    void updateStep2_rejectsNeitherFileNorFaceImageId() {
        assertThrows(ServiceException.class,
                () -> service.updateStep2(UUID.randomUUID(), null, null));
    }

    @Test
    void updateStep2_rejectsBackgroundImageType() {
        UUID videoId = UUID.randomUUID();
        UUID faceImageId = UUID.randomUUID();
        DeepfakeRenderJob job = step1CompleteJob();
        DeepfakeBackgroundImage background = DeepfakeBackgroundImage.builder()
                .backgroundImageId(faceImageId)
                .fileName("bg.png")
                .fileKey("deepfake/background/bg.png")
                .clientAdminId(CLIENT_ID)
                .imageType(DeepfakeImageType.BACKGROUND)
                .isActive(true)
                .isGlobal(false)
                .build();

        stubClient();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        when(backgroundImageRepository.findByBackgroundImageId(faceImageId)).thenReturn(Optional.of(background));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStep2(videoId, null, faceImageId));
    }

    @Test
    void updateStep2_rejectsOtherUsersFaceCapture() {
        UUID videoId = UUID.randomUUID();
        UUID faceImageId = UUID.randomUUID();
        DeepfakeRenderJob job = step1CompleteJob();
        DeepfakeBackgroundImage otherUser = DeepfakeBackgroundImage.builder()
                .backgroundImageId(faceImageId)
                .fileName("face.png")
                .fileKey("deepfake/face/other.jpg")
                .clientAdminId("other-client")
                .imageType(DeepfakeImageType.FACE_CAPTURE)
                .isActive(true)
                .isGlobal(false)
                .build();

        stubClient();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        when(backgroundImageRepository.findByBackgroundImageId(faceImageId)).thenReturn(Optional.of(otherUser));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStep2(videoId, null, faceImageId));
    }
}
