package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.enums.VoiceCloneSource;
import com.aspire.asat.phishing.dto.request.VishingVoiceFilter;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.exception.VoiceCloneLimitReachedException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.service.DeepfakeVoiceService;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.util.ProviderNameNormalizer;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepfakeVoiceServiceImpl implements DeepfakeVoiceService {

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("wav", "mp3", "m4a", "webm", "ogg", "flac");
    private static final String VISHING_SAMPLE_PREFIX = "vishing/voice-sample";

    private final DeepfakeS3Service deepfakeS3Service;
    private final VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    private final DeepfakeVoiceCloneRepository voiceCloneRepository;
    private final DeepfakeRenderJobRepository renderJobRepository;
    private final DeepfakeVideoService deepfakeVideoService;
    private final UserCurrentContextService userCurrentContextService;
    private final ProviderCredentialResolver providerCredentialResolver;
    private final MongoTemplate mongoTemplate;

    @Value("${deepfake.voice-clone.reuse-on-limit-reached:false}")
    private boolean reuseOnLimitReached;

    @Override
    public DeepfakeVideoStepResponse updateStep4(UUID videoId, MultipartFile audioSample,
                                                  String provider, String language, UUID voiceCloneId,
                                                  String voiceName, String providerId) {
        boolean hasSample = audioSample != null && !audioSample.isEmpty();
        boolean hasExisting = voiceCloneId != null;
        if (hasSample == hasExisting) {
            throw new ServiceException(
                    "Provide either an audio sample or an existing voiceCloneId, but not both",
                    HttpStatus.BAD_REQUEST);
        }

        DeepfakeRenderJob job = deepfakeVideoService.getForEdit(videoId);
        if (!job.isFaceConfirmed()) {
            throw new ServiceException("Face must be confirmed before voice cloning", HttpStatus.CONFLICT);
        }
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        return hasExisting
                ? reuseExistingVoice(job, clientId, voiceCloneId)
                : cloneNewVoice(job, clientId, audioSample, provider, language, voiceName, providerId);
    }

    private DeepfakeVideoStepResponse reuseExistingVoice(DeepfakeRenderJob job, String clientId, UUID voiceCloneId) {
        DeepfakeVoiceClone clone = voiceCloneRepository.findByVoiceCloneId(voiceCloneId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cloned voice not found: " + voiceCloneId));
        if (!clientId.equals(clone.getClientId())) {
            throw new ResourceNotFoundException("Cloned voice not found: " + voiceCloneId);
        }
        if (clone.getStatus() != DeepfakeJobStatus.COMPLETED
                || clone.getExternalVoiceId() == null || clone.getExternalVoiceId().isBlank()) {
            throw new ServiceException("Selected voice is not ready for reuse", HttpStatus.CONFLICT);
        }

        applyVoiceToJob(job, voiceCloneId);
        DeepfakeRenderJob saved = renderJobRepository.save(job);
        log.info("videoId={} reusing existing voiceCloneId={} for client={}", job.getRenderId(), voiceCloneId, clientId);
        return toStepResponse(saved);
    }

    private DeepfakeVideoStepResponse cloneNewVoice(DeepfakeRenderJob job, String clientId, MultipartFile audioSample,
                                                    String provider, String language, String voiceName,
                                                    String providerId) {
        validate(audioSample);
        String resolvedVoiceName = normalizeOptionalVoiceName(voiceName);
        String resolvedLanguage = normalizeLanguage(language != null ? language : job.getLanguage());

        VoiceCloneProvider resolvedProvider;
        ResolvedProviderCredentials credentials;
        String credentialId = null;
        VoiceCloneAdapter adapter;
        if (StringUtils.hasText(providerId)) {
            credentials = providerCredentialResolver.resolveById(clientId, providerId.trim());
            if (credentials.getCategory() != ProviderCategory.VOICE_CLONING) {
                throw new ServiceException(
                        "Provider credential is not a voice cloning provider", HttpStatus.BAD_REQUEST);
            }
            adapter = voiceCloneAdapterFactory.getAdapterForBaseUrl(credentials.getBaseUrl());
            resolvedProvider = adapter.getProvider();
            credentialId = providerId.trim();
        } else {
            resolvedProvider = StringUtils.hasText(provider)
                    ? parseVoiceCloneProvider(provider)
                    : VoiceCloneProvider.ELEVENLABS;
            credentials = providerCredentialResolver.resolve(clientId, resolvedProvider.name());
            adapter = voiceCloneAdapterFactory.getAdapter(resolvedProvider);
        }

        UUID voiceCloneId = UUID.randomUUID();
        String sampleKey = deepfakeS3Service.uploadMultipart(audioSample, "deepfake/voice-sample");

        DeepfakeVoiceClone entity = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(clientId)
                .provider(resolvedProvider)
                .providerCredentialId(credentialId)
                .voiceCloneSource(VoiceCloneSource.DEEPFAKE_VIDEO)
                .sampleS3Key(sampleKey)
                .voiceName(resolvedVoiceName)
                .sampleFileName(audioSample.getOriginalFilename())
                .language(resolvedLanguage)
                .status(DeepfakeJobStatus.PROCESSING)
                .build();
        voiceCloneRepository.save(entity);

        File tempFile = null;
        try {
            tempFile = deepfakeS3Service.downloadToTemp(sampleKey);
            String externalVoiceId = resolveExternalVoiceId(
                    adapter, tempFile, resolvedProvider, entity, entity.getLanguage(), credentials);

            entity.setExternalVoiceId(externalVoiceId);
            entity.setStatus(DeepfakeJobStatus.COMPLETED);
            entity.setFailureReason(null);
            voiceCloneRepository.save(entity);

            applyVoiceToJob(job, voiceCloneId);
            DeepfakeRenderJob saved = renderJobRepository.save(job);
            return toStepResponse(saved);
        } catch (Exception e) {
            entity.setStatus(DeepfakeJobStatus.FAILED);
            entity.setFailureReason(e.getMessage());
            voiceCloneRepository.save(entity);
            log.error("voiceCloneId={} cloning failed for videoId={}", voiceCloneId, job.getRenderId(), e);
            throw e instanceof ServiceException se
                    ? se
                    : new ServiceException("Voice cloning failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY, e);
        } finally {
            cleanup(tempFile);
        }
    }

    private void applyVoiceToJob(DeepfakeRenderJob job, UUID voiceCloneId) {
        job.setVoiceCloneId(voiceCloneId.toString());
        job.setCurrentStep(Math.max(job.getCurrentStep(), 4));
        job.setAudioS3Key(null);
        job.setVideoUrl(null);
        job.setVideoS3Key(null);
        job.setThumbnailS3Key(null);
        job.setFailureReason(null);
        job.reopenForEdit();
    }

    private DeepfakeVideoStepResponse toStepResponse(DeepfakeRenderJob job) {
        return DeepfakeVideoStepResponse.builder()
                .videoId(job.getRenderId().toString())
                .currentStep(job.getCurrentStep())
                .status(job.getStatus())
                .build();
    }

    @Override
    public Page<ClonedVoiceDto> listClonedVoices(VoiceCloneProvider provider, LocalDate createdAfter,
                                                 LocalDate createdBefore, int offset, int pageSize) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeSize = pageSize > 0 ? pageSize : 10;
        int safePage = Math.max(0, offset);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Query query = new Query();
        query.addCriteria(Criteria.where("clientId").is(clientId));
        query.addCriteria(Criteria.where("status").is(DeepfakeJobStatus.COMPLETED));
        query.addCriteria(Criteria.where("externalVoiceId").ne(null));
        query.addCriteria(Criteria.where("isDeleted").ne(true));
        if (provider != null) {
            query.addCriteria(Criteria.where("provider").is(provider));
        }
        applyCreatedRange(query, createdAfter, createdBefore);

        long total = mongoTemplate.count(query, DeepfakeVoiceClone.class);
        query.with(pageable);
        List<ClonedVoiceDto> items = mongoTemplate.find(query, DeepfakeVoiceClone.class).stream()
                .map(this::toClonedVoiceDto)
                .toList();
        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public Page<ClonedVoiceDto> listVishingVoices(VishingVoiceFilter filter, int offset, int pageSize) {
        VishingVoiceFilter resolved = filter != null ? filter : VishingVoiceFilter.builder().build();
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        boolean isPlatformAdmin = isPlatformAdmin(ctx.getUserType());

        int safeSize = pageSize > 0 ? pageSize : 10;
        int safePage = Math.max(0, offset);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(new Criteria().orOperator(
                Criteria.where("voiceCloneSource").is(VoiceCloneSource.VISHING),
                Criteria.where("sampleS3Key").regex("^" + Pattern.quote(VISHING_SAMPLE_PREFIX))));
        andCriteria.add(Criteria.where("isDeleted").ne(true));

        if (!isPlatformAdmin) {
            andCriteria.add(Criteria.where("clientId").is(ctx.getClientAdminId()));
        } else if (StringUtils.hasText(resolved.getClientId())) {
            andCriteria.add(Criteria.where("clientId").is(resolved.getClientId().trim()));
        }

        if (resolved.getProvider() != null) {
            andCriteria.add(Criteria.where("provider").is(resolved.getProvider()));
        }
        if (StringUtils.hasText(resolved.getLanguage())) {
            andCriteria.add(Criteria.where("language")
                    .is(resolved.getLanguage().trim().toLowerCase(Locale.ROOT)));
        }
        if (resolved.getStatus() != null) {
            andCriteria.add(Criteria.where("status").is(resolved.getStatus()));
        }
        if (StringUtils.hasText(resolved.getSearch())) {
            String escaped = Pattern.quote(resolved.getSearch().trim());
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("voiceName").regex(escaped, "i"),
                    Criteria.where("sampleFileName").regex(escaped, "i"),
                    Criteria.where("externalVoiceId").regex(escaped, "i")));
        }
        applyCreatedRangeCriteria(andCriteria, resolved.getCreatedAfter(), resolved.getCreatedBefore());

        Query query = new Query(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, DeepfakeVoiceClone.class);
        query.with(pageable);
        List<ClonedVoiceDto> items = mongoTemplate.find(query, DeepfakeVoiceClone.class).stream()
                .map(this::toClonedVoiceDto)
                .toList();
        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public void deleteClonedVoice(UUID voiceCloneId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeVoiceClone clone = voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, clientId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cloned voice not found: " + voiceCloneId));

        if (clone.getStatus() == DeepfakeJobStatus.PROCESSING) {
            throw new ServiceException("Voice cannot be deleted while cloning is in progress", HttpStatus.CONFLICT);
        }

        String externalVoiceId = clone.getExternalVoiceId();
        if (StringUtils.hasText(externalVoiceId) && clone.getProvider() != null) {
            long sharedCount = voiceCloneRepository
                    .countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
                            externalVoiceId, clone.getProvider(), voiceCloneId);
            if (sharedCount == 0) {
                VoiceCloneAdapter adapter = voiceCloneAdapterFactory.getAdapter(clone.getProvider());
                ResolvedProviderCredentials credentials = resolveVoiceCredentials(clientId, clone);
                adapter.deleteClone(externalVoiceId, credentials);
            } else {
                log.info("voiceCloneId={} skipping provider delete; {} other non-deleted clone(s) share externalVoiceId={}",
                        voiceCloneId, sharedCount, externalVoiceId);
            }
        }

        clone.setDeleted(true);
        voiceCloneRepository.save(clone);
        log.info("voiceCloneId={} soft-deleted for client={}", voiceCloneId, clientId);
    }

    private boolean isPlatformAdmin(String userType) {
        if (!StringUtils.hasText(userType)) {
            return false;
        }
        try {
            UserType type = UserType.fromString(userType);
            return type == UserType.ASPIRE_ADMIN
                    || type == UserType.SUPER_ADMIN
                    || type == UserType.SYSTEM_USER;
        } catch (Exception e) {
            return false;
        }
    }

    private void applyCreatedRange(Query query, LocalDate createdAfter, LocalDate createdBefore) {
        List<Criteria> criteria = new ArrayList<>();
        applyCreatedRangeCriteria(criteria, createdAfter, createdBefore);
        for (Criteria c : criteria) {
            query.addCriteria(c);
        }
    }

    private void applyCreatedRangeCriteria(List<Criteria> andCriteria, LocalDate createdAfter, LocalDate createdBefore) {
        Instant from = createdAfter == null ? null : createdAfter.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = createdBefore == null ? null : createdBefore.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (from != null && to != null) {
            andCriteria.add(Criteria.where("createdAt").gte(from).lt(to));
        } else if (from != null) {
            andCriteria.add(Criteria.where("createdAt").gte(from));
        } else if (to != null) {
            andCriteria.add(Criteria.where("createdAt").lt(to));
        }
    }

    private ClonedVoiceDto toClonedVoiceDto(DeepfakeVoiceClone clone) {
        return ClonedVoiceDto.builder()
                .voiceCloneId(clone.getVoiceCloneId() != null ? clone.getVoiceCloneId().toString() : null)
                .voiceName(clone.getVoiceName())
                .provider(clone.getProvider())
                .fileName(resolveFileName(clone))
                .sampleUrl(resolveSampleUrl(clone.getSampleS3Key()))
                .language(clone.getLanguage())
                .status(clone.getStatus())
                .usedFallbackVoice(clone.isUsedFallbackVoice())
                .createdAt(clone.getCreatedAt())
                .build();
    }

    /**
     * Prefer persisted {@code sampleFileName}; for legacy rows fall back to the safe name
     * embedded in the S3 key ({@code prefix/{uuid}-{safeName}}).
     */
    private String resolveFileName(DeepfakeVoiceClone clone) {
        if (clone.getSampleFileName() != null && !clone.getSampleFileName().isBlank()) {
            return clone.getSampleFileName();
        }
        return deriveFileNameFromS3Key(clone.getSampleS3Key());
    }

    private String deriveFileNameFromS3Key(String sampleS3Key) {
        if (sampleS3Key == null || sampleS3Key.isBlank()) {
            return null;
        }
        int slash = sampleS3Key.lastIndexOf('/');
        String segment = slash >= 0 ? sampleS3Key.substring(slash + 1) : sampleS3Key;
        // UUID (36 chars) + '-' separator
        if (segment.length() > 37 && segment.charAt(36) == '-') {
            return segment.substring(37);
        }
        return segment.isBlank() ? null : segment;
    }

    private String resolveSampleUrl(String sampleS3Key) {
        if (sampleS3Key == null || sampleS3Key.isBlank()) {
            return null;
        }
        return deepfakeS3Service.presignGetUrl(sampleS3Key);
    }

    /**
     * Creates a fresh clone for the sample. When the provider rejects the request
     * because the account's custom-voice quota is exhausted, optionally reuses the
     * most recent successfully cloned voice for the same provider when
     * {@code deepfake.voice-clone.reuse-on-limit-reached} is enabled; otherwise fails loudly.
     */
    private String resolveExternalVoiceId(VoiceCloneAdapter adapter, File sampleFile,
                                          VoiceCloneProvider provider, DeepfakeVoiceClone entity,
                                          String language, ResolvedProviderCredentials credentials) {
        try {
            return adapter.createClone(sampleFile, "deepfake-" + entity.getVoiceCloneId(), language, credentials);
        } catch (VoiceCloneLimitReachedException e) {
            if (!reuseOnLimitReached) {
                throw new ServiceException(
                        "Voice cloning limit reached for " + provider
                                + "; enable deepfake.voice-clone.reuse-on-limit-reached or free a custom voice slot",
                        HttpStatus.SERVICE_UNAVAILABLE, e);
            }
            String reused = voiceCloneRepository
                    .findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(
                            provider, DeepfakeJobStatus.COMPLETED)
                    .filter(c -> !c.isDeleted())
                    .map(DeepfakeVoiceClone::getExternalVoiceId)
                    .orElse(null);
            if (reused == null) {
                throw new ServiceException("Voice cloning limit reached and no previously cloned "
                        + provider + " voice is available to reuse", HttpStatus.SERVICE_UNAVAILABLE, e);
            }
            entity.setUsedFallbackVoice(true);
            log.warn("voiceCloneId={} reusing externalVoiceId={} because {} voice limit is reached",
                    entity.getVoiceCloneId(), reused, provider);
            return reused;
        }
    }

    private ResolvedProviderCredentials resolveVoiceCredentials(String clientId, DeepfakeVoiceClone clone) {
        if (StringUtils.hasText(clone.getProviderCredentialId())) {
            return providerCredentialResolver.resolveById(clientId, clone.getProviderCredentialId());
        }
        return providerCredentialResolver.resolve(clientId, clone.getProvider().name());
    }

    private VoiceCloneProvider parseVoiceCloneProvider(String providerName) {
        if (!StringUtils.hasText(providerName)) {
            throw new ServiceException("Provider credential has no providerName", HttpStatus.BAD_REQUEST);
        }
        VoiceCloneProvider parsed = ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, providerName);
        if (parsed == null) {
            throw new ServiceException(
                    "Unsupported voice cloning provider: " + providerName.trim(), HttpStatus.BAD_REQUEST);
        }
        return parsed;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Audio sample is required", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException("Audio size must be <= 20MB", HttpStatus.BAD_REQUEST);
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ServiceException("Invalid file name", HttpStatus.BAD_REQUEST);
        }
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("Invalid audio format. Allowed: wav, mp3, m4a", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeOptionalVoiceName(String voiceName) {
        if (!StringUtils.hasText(voiceName)) {
            return null;
        }
        String trimmed = voiceName.trim();
        // Defense: if Spring already joined identical repeated form fields ("name,name"), collapse them.
        String[] parts = trimmed.split("\\s*,\\s*");
        if (parts.length > 1) {
            boolean allSame = true;
            for (int i = 1; i < parts.length; i++) {
                if (!parts[0].equals(parts[i])) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) {
                return parts[0];
            }
        }
        return trimmed;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en";
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private void cleanup(File tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (Exception ex) {
            tempFile.deleteOnExit();
        }
    }
}
