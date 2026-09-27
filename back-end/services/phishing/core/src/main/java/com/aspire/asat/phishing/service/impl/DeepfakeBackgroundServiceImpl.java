package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.response.BackgroundImageDto;
import com.aspire.asat.phishing.dto.response.BackgroundUploadResponse;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import com.aspire.asat.phishing.repository.DeepfakeBackgroundImageRepository;
import com.aspire.asat.phishing.service.DeepfakeBackgroundService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DeepfakeBackgroundServiceImpl implements DeepfakeBackgroundService {

    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final DeepfakeS3Service deepfakeS3Service;
    private final DeepfakeBackgroundImageRepository backgroundImageRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final MongoTemplate mongoTemplate;

    @Override
    public BackgroundUploadResponse uploadBackground(MultipartFile backgroundImage) {
        validate(backgroundImage);
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String backgroundKey = deepfakeS3Service.uploadMultipart(backgroundImage, "deepfake/background");

        UUID backgroundImageId = UUID.randomUUID();
        DeepfakeBackgroundImage entity = DeepfakeBackgroundImage.builder()
                .backgroundImageId(backgroundImageId)
                .fileName(backgroundImage.getOriginalFilename())
                .fileKey(backgroundKey)
                .clientAdminId(clientAdminId)
                .imageType(DeepfakeImageType.BACKGROUND)
                .status(BackgroundImageStatus.ACTIVE)
                .isActive(true)
                .isGlobal(false)
                .build();
        backgroundImageRepository.save(entity);

        return BackgroundUploadResponse.builder()
                .id(backgroundImageId)
                .backgroundKey(backgroundKey)
                .backgroundPreviewUrl(deepfakeS3Service.presignGetUrl(backgroundKey))
                .build();
    }

    @Override
    public Page<BackgroundImageDto> listBackgrounds(int offset, int pageSize, String fileName,
                                                    LocalDate uploadDate, Boolean isActive, Boolean isGlobal,
                                                    DeepfakeImageType imageType) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeSize = pageSize > 0 ? pageSize : 10;
        int safePage = Math.max(0, offset);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(visibilityCriteria(clientAdminId, isGlobal, imageType));
        if (imageType != null) {
            andCriteria.add(imageTypeCriteria(imageType));
        }

        if (StringUtils.hasText(fileName)) {
            andCriteria.add(Criteria.where("fileName")
                    .regex(Pattern.quote(fileName.trim()), "i"));
        }
        if (uploadDate != null) {
            Instant from = uploadDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant to = uploadDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            andCriteria.add(Criteria.where("createdAt").gte(from).lt(to));
        }
        if (isActive != null) {
            andCriteria.add(Criteria.where("isActive").is(isActive));
        }

        Query query = new Query(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, DeepfakeBackgroundImage.class);
        query.with(pageable);
        List<BackgroundImageDto> items = mongoTemplate.find(query, DeepfakeBackgroundImage.class).stream()
                .map(this::toDto)
                .toList();
        return new PageImpl<>(items, pageable, total);
    }

    /**
     * Default: user's own non-global images OR global images.
     * FACE_CAPTURE: user's own face captures only (no globals).
     * When isGlobal filter is set, narrow to only that subset (may yield empty for FACE_CAPTURE + isGlobal=true).
     */
    private Criteria visibilityCriteria(String clientAdminId, Boolean isGlobal, DeepfakeImageType imageType) {
        Criteria userOwned = new Criteria().andOperator(
                Criteria.where("clientAdminId").is(clientAdminId),
                Criteria.where("isGlobal").is(false));
        Criteria global = new Criteria().andOperator(
                Criteria.where("isGlobal").is(true),
                Criteria.where("clientAdminId").is(null));

        if (imageType == DeepfakeImageType.FACE_CAPTURE) {
            if (Boolean.TRUE.equals(isGlobal)) {
                // Face captures are never global — force empty match
                return Criteria.where("_id").is("__none__");
            }
            return userOwned;
        }

        if (Boolean.TRUE.equals(isGlobal)) {
            return global;
        }
        if (Boolean.FALSE.equals(isGlobal)) {
            return userOwned;
        }
        return new Criteria().orOperator(userOwned, global);
    }

    /**
     * BACKGROUND also matches documents missing imageType (legacy rows).
     */
    private Criteria imageTypeCriteria(DeepfakeImageType imageType) {
        if (imageType == DeepfakeImageType.BACKGROUND) {
            return new Criteria().orOperator(
                    Criteria.where("imageType").is(DeepfakeImageType.BACKGROUND),
                    Criteria.where("imageType").exists(false),
                    Criteria.where("imageType").is(null));
        }
        return Criteria.where("imageType").is(imageType);
    }

    private BackgroundImageDto toDto(DeepfakeBackgroundImage image) {
        DeepfakeImageType type = image.getImageType() != null
                ? image.getImageType()
                : DeepfakeImageType.BACKGROUND;
        return BackgroundImageDto.builder()
                .id(image.getBackgroundImageId())
                .fileName(image.getFileName())
                .fileKey(image.getFileKey())
                .url(deepfakeS3Service.presignGetUrl(image.getFileKey()))
                .imageType(type)
                .isActive(image.isActive())
                .isGlobal(image.isGlobal())
                .status(image.getStatus())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Background image is required", HttpStatus.BAD_REQUEST);
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
}
