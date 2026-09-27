package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.response.BackgroundImageDto;
import com.aspire.asat.phishing.dto.response.BackgroundUploadResponse;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeBackgroundServiceImplTest {

    private static final String CLIENT_ID = "client-admin-1";
    private static final String FILE_KEY = "deepfake/background/abc-office.png";
    private static final String PREVIEW_URL = "https://s3.example/presigned";

    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private DeepfakeBackgroundImageRepository backgroundImageRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DeepfakeBackgroundServiceImpl service;

    private void stubClient() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    private MultipartFile validPng(String fileName, long size) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(size);
        when(file.getOriginalFilename()).thenReturn(fileName);
        return file;
    }

    @Test
    void uploadBackground_persistsEntity_andReturnsIdAndUrls() {
        stubClient();
        MultipartFile file = validPng("office.png", 1024);
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/background")).thenReturn(FILE_KEY);
        when(deepfakeS3Service.presignGetUrl(FILE_KEY)).thenReturn(PREVIEW_URL);
        when(backgroundImageRepository.save(any(DeepfakeBackgroundImage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BackgroundUploadResponse response = service.uploadBackground(file);

        assertNotNull(response.getId());
        assertEquals(FILE_KEY, response.getBackgroundKey());
        assertEquals(PREVIEW_URL, response.getBackgroundPreviewUrl());

        ArgumentCaptor<DeepfakeBackgroundImage> captor = ArgumentCaptor.forClass(DeepfakeBackgroundImage.class);
        verify(backgroundImageRepository).save(captor.capture());
        DeepfakeBackgroundImage saved = captor.getValue();
        assertEquals(response.getId(), saved.getBackgroundImageId());
        assertEquals("office.png", saved.getFileName());
        assertEquals(FILE_KEY, saved.getFileKey());
        assertEquals(CLIENT_ID, saved.getClientAdminId());
        assertEquals(DeepfakeImageType.BACKGROUND, saved.getImageType());
        assertEquals(BackgroundImageStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.isActive());
        assertFalse(saved.isGlobal());
    }

    @Test
    void uploadBackground_rejectsEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(ServiceException.class, () -> service.uploadBackground(file));
    }

    @Test
    void uploadBackground_rejectsOversizedFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(11L * 1024 * 1024);

        assertThrows(ServiceException.class, () -> service.uploadBackground(file));
    }

    @Test
    void uploadBackground_rejectsInvalidExtension() {
        MultipartFile file = validPng("bg.gif", 1024);

        assertThrows(ServiceException.class, () -> service.uploadBackground(file));
    }

    @Test
    void listBackgrounds_mapsResults_withPresignedUrl() {
        stubClient();
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-07-30T10:00:00Z");
        DeepfakeBackgroundImage entity = DeepfakeBackgroundImage.builder()
                .backgroundImageId(id)
                .fileName("office.png")
                .fileKey(FILE_KEY)
                .clientAdminId(CLIENT_ID)
                .imageType(DeepfakeImageType.BACKGROUND)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .createdAt(createdAt)
                .build();

        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of(entity));
        when(deepfakeS3Service.presignGetUrl(FILE_KEY)).thenReturn(PREVIEW_URL);

        Page<BackgroundImageDto> page = service.listBackgrounds(0, 10, null, null, null, null, null);

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        BackgroundImageDto dto = page.getContent().get(0);
        assertEquals(id, dto.getId());
        assertEquals("office.png", dto.getFileName());
        assertEquals(FILE_KEY, dto.getFileKey());
        assertEquals(PREVIEW_URL, dto.getUrl());
        assertEquals(DeepfakeImageType.BACKGROUND, dto.getImageType());
        assertTrue(dto.isActive());
        assertFalse(dto.isGlobal());
        assertEquals(BackgroundImageStatus.ACTIVE, dto.getStatus());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void listBackgrounds_defaultsMissingImageTypeToBackground() {
        stubClient();
        DeepfakeBackgroundImage legacy = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName("legacy.png")
                .fileKey(FILE_KEY)
                .clientAdminId(CLIENT_ID)
                .imageType(null)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .createdAt(Instant.now())
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of(legacy));
        when(deepfakeS3Service.presignGetUrl(FILE_KEY)).thenReturn(PREVIEW_URL);

        Page<BackgroundImageDto> page = service.listBackgrounds(0, 10, null, null, null, null, null);

        assertEquals(DeepfakeImageType.BACKGROUND, page.getContent().get(0).getImageType());
    }

    @Test
    void listBackgrounds_defaultsPageSizeWhenInvalid() {
        stubClient();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of());

        Page<BackgroundImageDto> page = service.listBackgrounds(0, 0, null, null, null, null, null);

        assertEquals(10, page.getSize());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void listBackgrounds_appliesFileNameAndUploadDateFilters() {
        stubClient();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of());

        Page<BackgroundImageDto> page =
                service.listBackgrounds(0, 10, "office", LocalDate.of(2026, 7, 30), true, false, null);

        assertEquals(0, page.getTotalElements());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(DeepfakeBackgroundImage.class));
        assertNotNull(queryCaptor.getValue());
        assertFalse(queryCaptor.getValue().getQueryObject().isEmpty());
        verify(mongoTemplate).find(any(Query.class), eq(DeepfakeBackgroundImage.class));
    }

    @Test
    void listBackgrounds_isGlobalTrue_queriesGlobalsOnly() {
        stubClient();
        DeepfakeBackgroundImage global = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName("global.png")
                .fileKey("deepfake/background/global.png")
                .clientAdminId(null)
                .imageType(DeepfakeImageType.BACKGROUND)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(true)
                .createdAt(Instant.now())
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of(global));
        when(deepfakeS3Service.presignGetUrl(global.getFileKey())).thenReturn(PREVIEW_URL);

        Page<BackgroundImageDto> page = service.listBackgrounds(0, 10, null, null, null, true, null);

        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent().get(0).isGlobal());
    }

    @Test
    void listBackgrounds_imageTypeBackground_returnsBackgrounds() {
        stubClient();
        DeepfakeBackgroundImage bg = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName("office.png")
                .fileKey(FILE_KEY)
                .clientAdminId(CLIENT_ID)
                .imageType(DeepfakeImageType.BACKGROUND)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .createdAt(Instant.now())
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of(bg));
        when(deepfakeS3Service.presignGetUrl(FILE_KEY)).thenReturn(PREVIEW_URL);

        Page<BackgroundImageDto> page =
                service.listBackgrounds(0, 10, null, null, null, null, DeepfakeImageType.BACKGROUND);

        assertEquals(1, page.getContent().size());
        assertEquals(DeepfakeImageType.BACKGROUND, page.getContent().get(0).getImageType());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(DeepfakeBackgroundImage.class));
        assertFalse(queryCaptor.getValue().getQueryObject().isEmpty());
    }

    @Test
    void listBackgrounds_imageTypeFaceCapture_returnsUserFacesOnly() {
        stubClient();
        DeepfakeBackgroundImage face = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName("face.jpg")
                .fileKey("deepfake/face/abc.jpg")
                .clientAdminId(CLIENT_ID)
                .imageType(DeepfakeImageType.FACE_CAPTURE)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .createdAt(Instant.now())
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeBackgroundImage.class))).thenReturn(List.of(face));
        when(deepfakeS3Service.presignGetUrl(face.getFileKey())).thenReturn(PREVIEW_URL);

        Page<BackgroundImageDto> page =
                service.listBackgrounds(0, 10, null, null, null, null, DeepfakeImageType.FACE_CAPTURE);

        assertEquals(1, page.getContent().size());
        assertEquals(DeepfakeImageType.FACE_CAPTURE, page.getContent().get(0).getImageType());
        assertFalse(page.getContent().get(0).isGlobal());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(DeepfakeBackgroundImage.class));
        assertFalse(queryCaptor.getValue().getQueryObject().isEmpty());
    }
}
