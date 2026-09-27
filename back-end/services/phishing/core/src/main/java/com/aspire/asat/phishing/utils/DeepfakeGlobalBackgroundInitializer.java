package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seeds global deepfake background images on startup (insert-if-missing by fileKey).
 * Objects already exist in S3 under deepfake/background/.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeepfakeGlobalBackgroundInitializer implements CommandLineRunner {

    private static final List<String> GLOBAL_FILE_KEYS = List.of(
            "deepfake/background/greenscreen-bg.png",
            "deepfake/background/multicolor-bg.png",
            "deepfake/background/newsroom-bg.png",
            "deepfake/background/stodio-bg.png",
            "deepfake/background/boardroom-bg.png",
            "deepfake/background/gradient-bg.png"
    );

    private final DeepfakeBackgroundImageRepository backgroundImageRepository;

    @Override
    public void run(String... args) {
        log.info("Starting deepfake global background image initialization...");
        int inserted = 0;
        for (String fileKey : GLOBAL_FILE_KEYS) {
            try {
                if (seedIfMissing(fileKey)) {
                    inserted++;
                }
            } catch (Exception e) {
                log.error("Failed to seed global background image: {}", fileKey, e);
            }
        }
        log.info("Deepfake global background initialization completed. inserted={}", inserted);
    }

    private boolean seedIfMissing(String fileKey) {
        if (backgroundImageRepository.existsByFileKeyAndIsGlobalTrue(fileKey)) {
            log.debug("Global background '{}' already exists, skipping.", fileKey);
            return false;
        }

        String fileName = fileKey.substring(fileKey.lastIndexOf('/') + 1);
        DeepfakeBackgroundImage image = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName(fileName)
                .fileKey(fileKey)
                .clientAdminId(null)
                .imageType(DeepfakeImageType.BACKGROUND)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(true)
                .createdAt(Instant.now())
                .build();
        backgroundImageRepository.save(image);
        log.info("Seeded global background image: fileKey={} id={}", fileKey, image.getBackgroundImageId());
        return true;
    }
}
