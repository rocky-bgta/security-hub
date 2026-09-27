package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeGlobalBackgroundInitializerTest {

    @Mock
    private DeepfakeBackgroundImageRepository backgroundImageRepository;

    @InjectMocks
    private DeepfakeGlobalBackgroundInitializer initializer;

    @Test
    void run_insertsMissingGlobalBackgrounds() {
        when(backgroundImageRepository.existsByFileKeyAndIsGlobalTrue(anyString())).thenReturn(false);
        when(backgroundImageRepository.save(any(DeepfakeBackgroundImage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        initializer.run();

        verify(backgroundImageRepository, times(6)).save(any(DeepfakeBackgroundImage.class));

        ArgumentCaptor<DeepfakeBackgroundImage> captor = ArgumentCaptor.forClass(DeepfakeBackgroundImage.class);
        verify(backgroundImageRepository, times(6)).save(captor.capture());
        DeepfakeBackgroundImage first = captor.getAllValues().get(0);
        assertEquals("deepfake/background/greenscreen-bg.png", first.getFileKey());
        assertEquals("greenscreen-bg.png", first.getFileName());
        assertNull(first.getClientAdminId());
        assertTrue(first.isGlobal());
        assertTrue(first.isActive());
        assertEquals(BackgroundImageStatus.ACTIVE, first.getStatus());
        assertEquals(DeepfakeImageType.BACKGROUND, first.getImageType());
    }

    @Test
    void run_skipsExistingGlobalBackgrounds() {
        when(backgroundImageRepository.existsByFileKeyAndIsGlobalTrue(anyString())).thenReturn(true);

        initializer.run();

        verify(backgroundImageRepository, never()).save(any(DeepfakeBackgroundImage.class));
    }
}
