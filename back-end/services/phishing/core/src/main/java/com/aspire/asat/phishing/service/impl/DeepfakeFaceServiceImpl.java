package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.service.DeepfakeFaceFramingService;
import com.aspire.asat.phishing.service.DeepfakeFaceService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.util.DeepfakeImageUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeepfakeFaceServiceImpl implements DeepfakeFaceService {

    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final DeepfakeS3Service deepfakeS3Service;
    private final DeepfakeVideoService deepfakeVideoService;
    private final DeepfakeRenderJobRepository renderJobRepository;
    private final DeepfakeFaceFramingService faceFramingService;
    private final DeepfakeBackgroundImageRepository backgroundImageRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public DeepfakeVideoStepResponse updateStep2(UUID videoId, MultipartFile faceImage, UUID faceImageId) {
        boolean hasFile = faceImage != null && !faceImage.isEmpty();
        boolean hasExisting = faceImageId != null;
        if (hasFile == hasExisting) {
            throw new ServiceException(
                    "Provide either a face image file or an existing faceImageId, but not both",
                    HttpStatus.BAD_REQUEST);
        }

        DeepfakeRenderJob job = deepfakeVideoService.getForEdit(videoId);
        ensureStep1Complete(job);

        if (hasExisting) {
            return reuseExistingFace(job, faceImageId);
        }
        return uploadNewFace(job, faceImage);
    }

    private DeepfakeVideoStepResponse reuseExistingFace(DeepfakeRenderJob job, UUID faceImageId) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeBackgroundImage image = backgroundImageRepository.findByBackgroundImageId(faceImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Face capture image not found: " + faceImageId));

        if (image.getImageType() != DeepfakeImageType.FACE_CAPTURE
                || image.isGlobal()
                || !clientAdminId.equals(image.getClientAdminId())
                || !image.isActive()) {
            throw new ResourceNotFoundException("Face capture image not found: " + faceImageId);
        }
        if (image.getFileKey() == null || image.getFileKey().isBlank()) {
            throw new ServiceException("Face capture image has no stored file", HttpStatus.CONFLICT);
        }

        applyFaceToJob(job, image.getFileKey());
        return toStepResponse(renderJobRepository.save(job));
    }

    private DeepfakeVideoStepResponse uploadNewFace(DeepfakeRenderJob job, MultipartFile faceImage) {
        validate(faceImage);

        byte[] faceBytes;
        try {
            faceBytes = faceImage.getBytes();
        } catch (Exception e) {
            throw new ServiceException("Unable to read face image", HttpStatus.BAD_REQUEST, e);
        }
        validateImageBytes(faceBytes);

        byte[] framedJpeg;
        try {
            framedJpeg = faceFramingService.frameToLandscape(faceBytes);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Unable to prepare face image for HeyGen. Upload a valid PNG or JPEG between "
                            + DeepfakeImageUtils.MIN_DIMENSION + " and "
                            + DeepfakeImageUtils.MAX_DIMENSION + " pixels per side.",
                    HttpStatus.BAD_REQUEST, e);
        }
        String faceKey = deepfakeS3Service.uploadBytes(
                framedJpeg, "deepfake/face", "jpg", DeepfakeImageUtils.JPEG);

        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeBackgroundImage entity = DeepfakeBackgroundImage.builder()
                .backgroundImageId(UUID.randomUUID())
                .fileName(faceImage.getOriginalFilename())
                .fileKey(faceKey)
                .clientAdminId(clientAdminId)
                .imageType(DeepfakeImageType.FACE_CAPTURE)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .build();
        backgroundImageRepository.save(entity);

        applyFaceToJob(job, faceKey);
        return toStepResponse(renderJobRepository.save(job));
    }

    private void applyFaceToJob(DeepfakeRenderJob job, String faceKey) {
        job.setFaceKey(faceKey);
        job.setFaceConfirmed(false);
        job.setHeygenAvatarId(null);
        job.setHeygenAvatarFaceKey(null);
        job.setCurrentStep(Math.max(job.getCurrentStep(), 2));
        job.reopenForEdit();
    }

    private DeepfakeVideoStepResponse toStepResponse(DeepfakeRenderJob saved) {
        return DeepfakeVideoStepResponse.builder()
                .videoId(saved.getRenderId().toString())
                .currentStep(saved.getCurrentStep())
                .status(saved.getStatus())
                .build();
    }

    private void ensureStep1Complete(DeepfakeRenderJob job) {
        if (job.getTitle() == null || job.getTitle().isBlank()) {
            throw new ServiceException("Complete step 1 (onboarding) before face capture", HttpStatus.CONFLICT);
        }
        if (job.getLanguage() == null || job.getLanguage().isBlank()) {
            throw new ServiceException("Complete step 1 (onboarding) before face capture", HttpStatus.CONFLICT);
        }
        if (job.getBackgroundType() == null) {
            throw new ServiceException("Complete step 1 (onboarding) before face capture", HttpStatus.CONFLICT);
        }
        if (job.getBackgroundType() == DeepfakeBackgroundType.PRESET && job.getBackgroundPreset() == null) {
            throw new ServiceException("Complete step 1 (onboarding) before face capture", HttpStatus.CONFLICT);
        }
        if (job.getBackgroundType() == DeepfakeBackgroundType.CUSTOM
                && (job.getBackgroundKey() == null || job.getBackgroundKey().isBlank())) {
            throw new ServiceException("Complete step 1 (onboarding) before face capture", HttpStatus.CONFLICT);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Face image is required", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException("Image size must be <= 10MB", HttpStatus.BAD_REQUEST);
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ServiceException("Invalid file name", HttpStatus.BAD_REQUEST);
        }
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("Invalid image format. Allowed: png, jpg", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateImageBytes(byte[] bytes) {
        if (DeepfakeImageUtils.isAvif(bytes)) {
            throw new ServiceException(
                    "AVIF images are not supported. Please upload a PNG or JPEG face photo.",
                    HttpStatus.BAD_REQUEST);
        }
        if (DeepfakeImageUtils.detectSupportedFaceContentType(bytes) == null) {
            throw new ServiceException(
                    "Invalid image format. Allowed: PNG or JPEG face photos only.",
                    HttpStatus.BAD_REQUEST);
        }
        if (DeepfakeImageUtils.readImageDimensions(bytes) == null) {
            throw new ServiceException(
                    "Face image has invalid or missing dimensions. Upload a valid PNG or JPEG photo.",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
