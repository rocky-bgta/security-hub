package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.CampaignTelephonyData;
import com.aspire.asat.phishing.dto.CampaignVoiceData;
import com.aspire.asat.phishing.dto.CampaignVoiceScenarioRef;
import com.aspire.asat.phishing.dto.TopicIdDetailDto;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.enums.VishingScenarioStatus;
import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.dto.cms.ClientAdminInfoDto;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageUpdateRequest;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterRequestDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.request.CampaignAudienceRequest;
import com.aspire.asat.phishing.dto.request.CampaignCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignEmailTemplateRequest;
import com.aspire.asat.phishing.dto.request.CampaignLandingPageRequest;
import com.aspire.asat.phishing.dto.request.CampaignScheduleRequest;
import com.aspire.asat.phishing.dto.request.CampaignSenderProfileRequest;
import com.aspire.asat.phishing.dto.request.CampaignSmsServerRequest;
import com.aspire.asat.phishing.dto.request.CampaignScenarioRequest;
import com.aspire.asat.phishing.dto.request.CampaignTelephonyRequest;
import com.aspire.asat.phishing.dto.request.CampaignVoiceSetupRequest;
import com.aspire.asat.phishing.dto.request.CampaignTagsRequest;
import com.aspire.asat.phishing.dto.request.CampaignTrainingRequest;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.dto.response.CampaignRecipientDto;
import com.aspire.asat.phishing.dto.response.CampaignRiskImpactDto;
import com.aspire.asat.phishing.dto.response.CampaignStatsDto;
import com.aspire.asat.phishing.dto.response.EndUserPhishingVisibilityDto;
import com.aspire.asat.phishing.dto.response.UserCampaignStatisticsDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignSchedule;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.model.VishingScenario;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.service.CampaignService;
import com.aspire.asat.phishing.service.CampaignVoiceCloneService;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.TopicRecommendationFilterAssembler;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.CampaignAudienceUserResolver;
import com.aspire.asat.phishing.service.support.CampaignTypeChannelValidator;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.service.support.RecipientResolverFactory;
import com.aspire.asat.phishing.service.support.SmsTemplateValidator;
import com.aspire.asat.phishing.service.support.VoiceResponseStageMapper;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.util.CampaignScheduleDateTimeParser;
import com.aspire.asat.phishing.util.ScheduleDateTimeParseResult;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for campaign management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final CampaignMapper campaignMapper;
    private final RegistrationServiceClient registrationClient;
    private final CmsSubPackageClient cmsSubPackageClient;
    private final CmsTopicClient cmsTopicClient;
    private final TopicRecommendationFilterAssembler topicRecommendationFilterAssembler;
    private final UserCurrentContextService userCurrentContextService;
    private final CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;
    private final EmailActivityRepository emailActivityRepository;
    private final CampaignAudienceUserResolver campaignAudienceUserResolver;
    private final RecipientResolverFactory recipientResolverFactory;
    private final SmsServerConfigurationRepository smsServerConfigurationRepository;
    private final SmsServerConfigurationService smsServerConfigurationService;
    private final VoiceServerConfigurationRepository voiceServerConfigurationRepository;
    private final VoiceServerConfigurationService voiceServerConfigurationService;
    private final VishingScenarioRepository vishingScenarioRepository;
    private final CampaignVoiceCloneService campaignVoiceCloneService;
    private final SmsTemplateValidator smsTemplateValidator;

    // Dependencies for validation
    private final EmailTemplateRepository emailTemplateRepository;
    private final LandingPageRepository landingPageRepository;
    private final SenderProfileRepository senderProfileRepository;
    private final UserRiskProfileRepository userRiskProfileRepository;
    private final UserRiskProfileService userRiskProfileService;
    private final CampaignUserRiskProfileAsyncUpdater campaignUserRiskProfileAsyncUpdater;
    private final CampaignScheduleDateTimeParser scheduleDateTimeParser;
    private final TrackingBaseUrlResolver trackingBaseUrlResolver;
    private final UrlShortenerService urlShortenerService;

    // --- CRUD Operations ---

    @Override
    public List<CampaignDto> getCampaigns(int offset, int pageSize, String searchParam,
                                           CampaignStatus status, CampaignChannel channel,
                                           String sortBy, String sortOrder) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "createdAt"
        );

        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        Pageable pageable = PageRequest.of(pageNumber, effectivePageSize, sort);

        Page<Campaign> campaigns = campaignRepository.findWithFilters(
                clientId, searchParam, status, channel, null, null, null, false, pageable);

        List<CampaignDto> list = campaigns.getContent().stream()
                .map(campaignMapper::toDto)
                .collect(Collectors.toList());
        enrichCampaignDtosBatch(list);
        return list;
    }

    @Override
    public long countCampaigns(String searchParam, CampaignStatus status, CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return campaignRepository.countWithFilters(
                clientId, searchParam, status, channel, null, null, null, false);
    }

    @Override
    public CampaignDto getCampaignById(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        return toCampaignDtoWithNames(campaign);
    }

    @Override
    public CampaignDto createCampaign(CampaignCreateRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        // Check for duplicate name
        if (campaignRepository.existsByClientIdAndCampaignName(clientId, request.getCampaignName())) {
            throw new DuplicateDataFoundException(MessageKeys.CAMPAIGN_NAME_ALREADY_EXISTS);
        }
        
        Campaign campaign = campaignMapper.toEntity(request, clientId);
        CampaignTypeChannelValidator.validate(request.getChannel(), request.getCampaignType());
        campaign.setChannel(request.getChannel() != null ? request.getChannel() : CampaignChannel.EMAIL);
        applyAssignedForFromRequest(campaign, request);
        campaign.setExpireDate(request.getExpireDate());
        campaign.setExpiresAt(computeExpiresAtFromRequest(campaign, request));
        validateExpiryAgainstSchedule(campaign.getExpiresAt(), campaign.getSchedule());
        Campaign saved = campaignRepository.save(campaign);
        
        log.info("Created campaign: {} for client: {}", saved.getId(), clientId);
        return campaignMapper.toDto(saved);
    }

    @Override
    public void deleteCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new PhishingValidationException("Only draft campaigns can be deleted");
        }
        
        // Delete recipients first
        recipientRepository.deleteByCampaignId(campaignId);
        campaignRepository.delete(campaign);
        
        log.info("Deleted campaign: {}", campaignId);
    }

    // --- Wizard Step Updates ---

    @Override
    public CampaignDto updateCampaignSetup(String campaignId, CampaignCreateRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        
        // Check for duplicate name (excluding current)
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (campaignRepository.existsByClientIdAndCampaignNameAndIdNot(clientId, request.getCampaignName(), campaignId)) {
            throw new DuplicateDataFoundException(MessageKeys.CAMPAIGN_NAME_ALREADY_EXISTS);
        }
        
        campaign.setCampaignName(request.getCampaignName());
        campaign.setCampaignType(request.getCampaignType());
        CampaignTypeChannelValidator.validate(request.getChannel(), request.getCampaignType());
        campaign.setChannel(request.getChannel() != null ? request.getChannel() : CampaignChannel.EMAIL);
        applyLicenseScopeFromRequest(campaign, request);
        applyAssignedForFromRequest(campaign, request);
        campaign.setExpireDate(request.getExpireDate());
        campaign.setExpiresAt(computeExpiresAtFromRequest(campaign, request));
        validateExpiryAgainstSchedule(campaign.getExpiresAt(), campaign.getSchedule());
        if (campaign.getTrainingData() != null && campaign.getAssignedFor() != null) {
            campaign.getTrainingData().setAssignedFor(campaign.getAssignedFor());
        }
        updateStepProgress(campaign, 1);

        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateEmailTemplate(String campaignId, CampaignEmailTemplateRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Use voice setup endpoint for voice campaigns");
        }
        
        // Validate template exists
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        EmailTemplate selectedTemplate = emailTemplateRepository
                .findByIdAndClientIdOrGlobal(request.getEmailTemplateId(), clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found"));

        CampaignChannel effectiveChannel = CampaignTypeChannelValidator.effectiveChannel(campaign.getChannel());
        if (selectedTemplate.getTemplateType() != null
                && effectiveChannel == CampaignChannel.SMS
                && selectedTemplate.getTemplateType() != TemplateType.SMS) {
            throw new PhishingValidationException("Selected template must be an SMS template");
        }
        if (effectiveChannel == CampaignChannel.EMAIL
                && selectedTemplate.getTemplateType() == TemplateType.SMS) {
            throw new PhishingValidationException("Selected template must be an email template");
        }

        if (selectedTemplate.isGlobal()) {
            selectedTemplate.setPopularity(selectedTemplate.getPopularity() + 1);
            emailTemplateRepository.save(selectedTemplate);
        }

        campaign.setEmailTemplateId(request.getEmailTemplateId());
        updateStepProgress(campaign, 2);

        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateLandingPage(String campaignId, CampaignLandingPageRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Use scenario endpoint for voice campaigns");
        }
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        LandingPage selectedLandingPage = null;

        // Validate landing page exists (if provided)
        if (request.getLandingPageId() != null) {
            selectedLandingPage = landingPageRepository
                    .findByIdAndClientIdOrGlobal(request.getLandingPageId(), clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));

            if (selectedLandingPage.isGlobal()) {
                selectedLandingPage.setPopularity(selectedLandingPage.getPopularity() + 1);
                landingPageRepository.save(selectedLandingPage);
            }
        }
        
        campaign.setLandingPageId(request.getLandingPageId());
        campaign.setLandingPageType(request.getLandingPageType());

        if (StringUtils.hasText(request.getTrackingDomainId())) {
            trackingBaseUrlResolver.validateDomainId(clientId, request.getTrackingDomainId());
            campaign.setTrackingDomainId(request.getTrackingDomainId().trim());
        } else if (selectedLandingPage != null && StringUtils.hasText(selectedLandingPage.getTrackingDomainId())) {
            campaign.setTrackingDomainId(selectedLandingPage.getTrackingDomainId());
        } else {
            campaign.setTrackingDomainId(null);
        }

        updateStepProgress(campaign, 3);

        return toCampaignDtoWithNames(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateSenderProfile(String campaignId, CampaignSenderProfileRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (CampaignTypeChannelValidator.effectiveChannel(campaign.getChannel()) == CampaignChannel.SMS) {
            throw new PhishingValidationException("Use SMS server configuration endpoint for SMS campaigns");
        }
        if (campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Use telephony endpoint for voice campaigns");
        }
        
        // Validate sender profile exists
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (!senderProfileRepository.findByIdAndClientIdOrGlobal(request.getSenderProfileId(), clientId).isPresent()) {
            throw new ResourceNotFoundException("Sender profile not found");
        }
        
        campaign.setSenderProfileId(request.getSenderProfileId());
        updateStepProgress(campaign, 4);
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateSmsServer(String campaignId, CampaignSmsServerRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (campaign.getChannel() != CampaignChannel.SMS) {
            throw new PhishingValidationException("SMS server configuration is only for SMS campaigns");
        }
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        smsServerConfigurationService.resolveForCampaign(clientId, request.getSmsServerConfigurationId());
        campaign.setSmsServerConfigurationId(request.getSmsServerConfigurationId());
        campaign.setSenderProfileId(null);
        updateStepProgress(campaign, 4);
        return toCampaignDtoWithNames(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateVoiceSetup(String campaignId,
                                        CampaignVoiceSetupRequest request,
                                        MultipartFile audioSample,
                                        UUID voiceCloneId,
                                        VoiceCloneProvider provider,
                                        String language,
                                        String voiceName) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (!campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Voice setup is only for voice campaigns");
        }
        if (!Boolean.TRUE.equals(request.getConsentConfirmed())) {
            throw new PhishingValidationException("Voice clone consent must be confirmed");
        }

        boolean hasFile = audioSample != null && !audioSample.isEmpty();
        if (voiceCloneId != null && hasFile) {
            throw new PhishingValidationException(
                    "Provide either an audio sample or an existing voiceCloneId, not both");
        }
        if (voiceCloneId == null && !hasFile) {
            throw new PhishingValidationException(
                    "Either an audio sample or an existing voiceCloneId is required");
        }

        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        DeepfakeVoiceClone clone = voiceCloneId != null
                ? campaignVoiceCloneService.getExistingClone(voiceCloneId, clientId)
                : campaignVoiceCloneService.createClone(audioSample, provider, language, clientId, voiceName);
        if (clone.getVoiceCloneId() == null || !StringUtils.hasText(clone.getExternalVoiceId())) {
            throw new ServiceException("Voice clone did not complete successfully", HttpStatus.BAD_GATEWAY);
        }

        String displayName = StringUtils.hasText(clone.getVoiceName())
                ? clone.getVoiceName()
                : clone.getExternalVoiceId();
        CampaignVoiceData voiceData = CampaignVoiceData.builder()
                .consentConfirmed(true)
                .consentText(request.getConsentText())
                .consentConfirmedAt(Instant.now())
                .voiceCloneId(clone.getVoiceCloneId().toString())
                .externalVoiceId(clone.getExternalVoiceId())
                .voiceDisplayName(displayName)
                .cloningEngine(clone.getProvider())
                .callerId(request.getCallerId())
                .usedFallbackVoice(clone.isUsedFallbackVoice())
                .build();
        campaign.setVoiceData(voiceData);
        updateStepProgress(campaign, 2);
        return toCampaignDtoWithNames(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateScenario(String campaignId, CampaignScenarioRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (!campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Scenario configuration is only for voice campaigns");
        }

        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        VishingScenario scenario = vishingScenarioRepository.findByIdAndClientIdOrGlobal(request.getScenarioId(), clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Vishing scenario not found"));
        if (scenario.getStatus() != VishingScenarioStatus.PUBLISHED) {
            throw new PhishingValidationException("Scenario must be published before use in a campaign");
        }

        if (scenario.isGlobal()) {
            scenario.setPopularity(scenario.getPopularity() + 1);
            vishingScenarioRepository.save(scenario);
        }

        CampaignVoiceScenarioRef ref = CampaignVoiceScenarioRef.builder()
                .scenarioId(scenario.getId())
                .scenarioName(scenario.getScenarioName())
                .attackTemplateId(scenario.getAttackTemplateId())
                .attackTemplateName(scenario.getAttackTemplateName())
                .language(scenario.getLanguage())
                .tone(scenario.getTone())
                .scriptBody(scenario.getScriptBody())
                .detectedVariables(scenario.getDetectedVariables())
                .enableLlmResponses(scenario.isEnableLlmResponses())
                .interactionMode(scenario.getInteractionMode() != null
                        ? scenario.getInteractionMode() : VishingInteractionMode.BOTH)
                .escalationLimit(scenario.getEscalationLimit())
                .build();
        campaign.setVoiceScenario(ref);
        updateStepProgress(campaign, 3);
        return toCampaignDtoWithNames(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateTelephony(String campaignId, CampaignTelephonyRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        if (!campaign.isVoiceChannel()) {
            throw new PhishingValidationException("Telephony configuration is only for voice campaigns");
        }

        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        voiceServerConfigurationService.resolveForCampaign(clientId, request.getVoiceServerConfigurationId());

        CampaignTelephonyData.RetryPolicy retryPolicy = null;
        if (request.getRetryPolicy() != null) {
            retryPolicy = CampaignTelephonyData.RetryPolicy.builder()
                    .maxRetries(request.getRetryPolicy().getMaxRetries())
                    .intervalMinutes(request.getRetryPolicy().getIntervalMinutes())
                    .build();
        }

        CampaignTelephonyData telephonyData = CampaignTelephonyData.builder()
                .voiceServerConfigurationId(request.getVoiceServerConfigurationId())
                .region(request.getRegion())
                .countryCode(request.getCountryCode())
                .retryPolicy(retryPolicy != null ? retryPolicy : CampaignTelephonyData.RetryPolicy.builder().build())
                .build();

        campaign.setVoiceServerConfigurationId(request.getVoiceServerConfigurationId());
        campaign.setTelephonyData(telephonyData);
        campaign.setSenderProfileId(null);
        campaign.setSmsServerConfigurationId(null);
        updateStepProgress(campaign, 4);
        return toCampaignDtoWithNames(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateTags(String campaignId, CampaignTagsRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        
        campaign.setCampaignTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        updateStepProgress(campaign, 5);

        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateAudience(String campaignId, CampaignAudienceRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        if (!StringUtils.hasText(campaign.getProductPackageId())) {
            throw new PhishingValidationException(
                    "productPackageId is required on the campaign before audience selection");
        }

        campaignMapper.applyAudienceRequest(campaign, request);

        // Calculate recipient count and create recipient records from licensed users
        int recipientCount = calculateAndCreateRecipients(campaign, request, clientId);
        campaign.getAudience().setRecipientCount(recipientCount);

        if (recipientCount == 0) {
            throw new PhishingValidationException("Campaign must have at least one recipient");
        }

        updateStepProgress(campaign, 6);
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto updateTraining(String campaignId, CampaignTrainingRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);

        if (!campaign.requiresTraining()) {
            campaign.setTrainingData(null);
        } else {
            validateTrainingStepRequest(request);

            // Generate SubPackage Name
            request.setName(campaign.getCampaignName() + " Training ");
            request.setDescription(request.getDescription() != null ? request.getDescription() : "Training for " + campaign.getCampaignName());

            String clientId = request.getClientId();
            SubPackageAssignedFor assignedFor = campaign.getAssignedFor();
            if (assignedFor == null) {
                throw new ResourceNotFoundException("assignedFor is required on campaign step 1 before training step");
            }

            CampaignTrainingData existing = campaign.getTrainingData();
            String subPackageId;

            if (existing != null && existing.getTrainingModuleId() != null) {
                cmsSubPackageClient.updateSubPackage(existing.getTrainingModuleId(),
                        CmsSubPackageUpdateRequest.builder()
                                .name(request.getName().trim())
                                .description(request.getDescription())
                                .topicId(request.getTopicId())
                                .assignedFor(assignedFor)
                                .channel(campaign.getChannel().name())
                                .build());
                subPackageId = existing.getTrainingModuleId();
            } else {
                String createdBy = userCurrentContextService.getCurrentUserContext().getUserId();
                CmsSubPackageCreateRequest cmsRequest = CmsSubPackageCreateRequest.builder()
                        .name(request.getName().trim())
                        .description(request.getDescription())
                        .productId(request.getProductId().trim())
                        .packageId(request.getPackageId().trim())
                        .productPackageId(request.getProductPackageId().trim())
                        .clientId(clientId)
                        .clientAdminId(clientId)
                        .topicId(request.getTopicId())
                        .createdBy(createdBy)
                        .status("ACTIVE")
                        .assignedFor(assignedFor)
                        .isTrial(false)
                        .showInSite(false)
                        .isPhishingSubpackage(true)
                        .channel(campaign.getChannel().name())
                        .build();
                subPackageId = cmsSubPackageClient.createSubPackage(cmsRequest);
            }

            campaign.setTrainingData(CampaignTrainingData.builder()
                    .trainingModuleId(subPackageId)
                    .name(request.getName().trim())
                    .description(request.getDescription())
                    .productId(request.getProductId().trim())
                    .packageId(request.getPackageId().trim())
                    .productPackageId(request.getProductPackageId().trim())
                    .clientId(clientId)
                    .topicId(request.getTopicId())
                    .topicIdDetails(request.getTopicIdDetails())
                    .assignedFor(assignedFor)
                    .completionDays(toStoredCompletionDays(request.getCompletionDays()))
                    .build());
            copyLicenseScopeFromTrainingData(campaign);
        }

        updateStepProgress(campaign, 7);
        Campaign saved = campaignRepository.save(campaign);
        return toCampaignDtoWithNames(saved);
    }

    private void validateTrainingStepRequest(CampaignTrainingRequest request) {
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new ResourceNotFoundException("productId is required for training sub-package");
        }
        if (request.getPackageId() == null || request.getPackageId().isBlank()) {
            throw new ResourceNotFoundException("packageId is required for training sub-package");
        }
        if (request.getProductPackageId() == null || request.getProductPackageId().isBlank()) {
            throw new ResourceNotFoundException("productPackageId is required for training sub-package");
        }
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            throw new ResourceNotFoundException("clientId is required for training sub-package");
        }
        if (request.getCompletionDays() == null) {
            throw new ResourceNotFoundException("completionDays is required for training sub-package");
        }
        if (request.getCompletionDays().getDurationUnit() == null || request.getCompletionDays().getDurationUnit().isBlank()) {
            throw new ResourceNotFoundException("completionDays.durationUnit is required for training sub-package");
        }
        if (request.getCompletionDays().getDurationValue() == null || request.getCompletionDays().getDurationValue() <= 0) {
            throw new ResourceNotFoundException("completionDays.durationValue must be greater than 0 for training sub-package");
        }
        List<String> topicIds = request.getTopicId();
        if (topicIds == null || topicIds.size() < 2) {
            throw new ResourceNotFoundException("At least two topic IDs are required for training sub-package");
        }
    }



    @Override
    public CampaignDto updateSchedule(String campaignId, CampaignScheduleRequest request) {
        Campaign campaign = getCampaignForEdit(campaignId);

        RegistrationServiceClient.RegistrationTimezone resolvedTimezone =
                resolveTimezone(request.getTimezone());
        String timeZoneDisplay = resolvedTimezone != null ? resolvedTimezone.displayName() : null;
        String ianaZoneId = resolvedTimezone != null ? resolvedTimezone.ianaZoneId() : null;

        Instant startDateTime;
        Instant endDateTime;

        if (request.getScheduleType() == ScheduleType.SCHEDULED) {
            if (request.getTimezone() == null || request.getTimezone().isBlank()) {
                throw new PhishingValidationException("Time zone is required for scheduled campaigns");
            }

            ScheduleDateTimeParseResult parsed = scheduleDateTimeParser.parseSchedule(
                    request.getStartDateTime(),
                    request.getEndDateTime(),
                    timeZoneDisplay,
                    ianaZoneId);
            startDateTime = parsed.getStartDateTime();
            endDateTime = parsed.getEndDateTime();

            if (startDateTime == null) {
                throw new PhishingValidationException("Start date/time is required for scheduled campaigns");
            }
            if (startDateTime.isBefore(Instant.now())) {
                throw new PhishingValidationException("Start time must be in the future");
            }
        } else {
            startDateTime = scheduleDateTimeParser.parse(
                    request.getStartDateTime(), timeZoneDisplay, ianaZoneId);
            endDateTime = scheduleDateTimeParser.parse(
                    request.getEndDateTime(), timeZoneDisplay, ianaZoneId);
        }

        if (campaign.getExpiresAt() != null && startDateTime != null
                && !campaign.getExpiresAt().isAfter(startDateTime)) {
            throw new PhishingValidationException("Campaign expiration must be after schedule start time");
        }

        campaignMapper.applyScheduleRequest(
                campaign, request, startDateTime, endDateTime, timeZoneDisplay);
        updateStepProgress(campaign, 8);

        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    private RegistrationServiceClient.RegistrationTimezone resolveTimezone(String timeZoneRef) {
        if (timeZoneRef == null || timeZoneRef.isBlank()) {
            return null;
        }
        String ref = timeZoneRef.trim();
        if (isRegistrationTimezoneDocumentId(ref)) {
            return registrationClient.getTimezoneByDocumentId(ref)
                    .orElseThrow(() -> new PhishingValidationException(
                            "Unable to resolve timezone id: " + ref));
        }
        return registrationClient.getTimezoneByTimezoneId(ref)
                .orElseGet(() -> new RegistrationServiceClient.RegistrationTimezone(ref, ref));
    }

    /** Registration dropdown document ids are UUIDs; other values are timezoneId / IANA / labels. */
    private static boolean isRegistrationTimezoneDocumentId(String value) {
        return value.matches("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }


    // --- Campaign Lifecycle ---

    @Override
    public CampaignDto launchCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        if (campaign.isExpired()) {
            if (campaign.getStatus() == CampaignStatus.RUNNING) {
                campaign.setStatus(CampaignStatus.EXPIRED);
                campaign.setCompletedAt(Instant.now());
                campaignRepository.save(campaign);
            }
            throw new PhishingValidationException("Campaign is expired and cannot be launched");
        }
        
        if (!campaign.canLaunch()) {
            throw new PhishingValidationException("Campaign cannot be launched. Complete all required steps first.");
        }

        ensureLicenseScopeForLaunch(campaign);

        List<CampaignRecipient> recipients = recipientRepository.findByCampaignId(campaignId);
        recipientResolverFactory.getResolver(campaign.getChannel()).validateBeforeLaunch(campaign, recipients);
        validateDeliveryConfiguration(campaign, clientId);

        // Update status based on schedule
        if (campaign.getSchedule() != null && campaign.getSchedule().getType() == ScheduleType.SCHEDULED) {
            campaign.setStatus(CampaignStatus.SCHEDULED);
        } else {
            campaign.setStatus(CampaignStatus.RUNNING);
            campaign.setLaunchedAt(Instant.now());
            campaignDeliveryOrchestrator.publishCampaign(campaign.getId());
        }
        
        // Initialize stats
        long recipientCount = recipientRepository.countByCampaignId(campaignId);
        campaign.getStats().setTotalRecipients((int) recipientCount);
        campaign.getStats().setLastUpdatedAt(Instant.now());
        
        updateStepProgress(campaign, 9);
        
        log.info("Launched campaign: {} with status: {}", campaignId, campaign.getStatus());
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto pauseCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        if (!campaign.canPause()) {
            throw new PhishingValidationException("Only running campaigns can be paused");
        }
        
        campaign.setStatus(CampaignStatus.PAUSED);
        campaign.setPausedAt(Instant.now());
        
        log.info("Paused campaign: {}", campaignId);
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto resumeCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        if (campaign.isExpired()) {
            if (campaign.getStatus() == CampaignStatus.RUNNING || campaign.getStatus() == CampaignStatus.PAUSED) {
                campaign.setStatus(CampaignStatus.EXPIRED);
                campaign.setCompletedAt(Instant.now());
                campaignRepository.save(campaign);
            }
            throw new PhishingValidationException("Expired campaigns cannot be resumed");
        }
        
        if (!campaign.canResume()) {
            throw new PhishingValidationException("Only paused campaigns can be resumed");
        }
        
        campaign.setStatus(CampaignStatus.RUNNING);
        
        log.info("Resumed campaign: {}", campaignId);
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    @Override
    public CampaignDto cancelCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        if (!campaign.canCancel()) {
            throw new PhishingValidationException("Campaign cannot be cancelled");
        }
        
        campaign.setStatus(CampaignStatus.CANCELLED);
        campaign.setCancelledAt(Instant.now());
        
        log.info("Cancelled campaign: {}", campaignId);
        return campaignMapper.toDto(campaignRepository.save(campaign));
    }

    // --- Recipients ---

    @Override
    public List<CampaignRecipientDto> getRecipients(String campaignId, int offset, int pageSize,
                                                     String searchParam, String sortBy, String sortOrder) {
        // Validate campaign access
        getCampaignById(campaignId);
        
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "email"
        );

        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        Pageable pageable = PageRequest.of(pageNumber, effectivePageSize, sort);

        Page<CampaignRecipient> recipients;
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            recipients = recipientRepository.searchRecipients(campaignId, searchParam.trim(), pageable);
        } else {
            recipients = recipientRepository.findByCampaignId(campaignId, pageable);
        }
        
        List<CampaignRecipientDto> recipientDtos = recipients.getContent().stream()
                .map(campaignMapper::toRecipientDto)
                .collect(Collectors.toList());

        List<String> recipientIds = recipientDtos.stream()
                .map(CampaignRecipientDto::getRecipientId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toList());

        if (!recipientIds.isEmpty()) {
            List<EmailActivity> activities = emailActivityRepository.findByCampaignIdsAndRecipientIds(
                    List.of(campaignId), recipientIds);

            // Since query is sorted by timestamp desc, first activity encountered per recipient is the latest.
            Map<String, String> latestActivityByRecipientId = new HashMap<>();
            Set<String> seenRecipients = new HashSet<>();
            for (EmailActivity activity : activities) {
                if (activity == null || activity.getRecipientId() == null || activity.getActivityType() == null) {
                    continue;
                }
                if (seenRecipients.add(activity.getRecipientId())) {
                    latestActivityByRecipientId.put(activity.getRecipientId(), activity.getActivityType().name());
                }
            }

            for (CampaignRecipientDto dto : recipientDtos) {
                if (dto == null || dto.getRecipientId() == null) {
                    continue;
                }
                dto.setActivity(latestActivityByRecipientId.get(dto.getRecipientId()));
            }
        }

        return recipientDtos;
    }

    @Override
    public long countRecipients(String campaignId, String searchParam) {
        // Validate campaign access
        getCampaignById(campaignId);
        
        return recipientRepository.countByCampaignId(campaignId);
    }

    @Override
    public String previewEmail(String campaignId, String recipientId) {
        // TODO: Implement email preview with personalization
        // - Get email template
        // - Get recipient data
        // - Replace placeholders with actual values
        return "<html><body>Email preview placeholder</body></html>";
    }

    // --- Statistics ---

    @Override
    public CampaignStatsDto getCampaignStats(String campaignId) {
        CampaignDto campaign = getCampaignById(campaignId);
        return campaign.getStats();
    }

    @Override
    public Page<CampaignRiskImpactDto> getCampaignRiskImpact(
            int offset,
            int pageSize,
            String searchParam,
            CampaignType type,
            CampaignStatus status,
            RiskLevel riskImpact,
            Instant startDate,
            Instant endDate,
            CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safePageSize = pageSize > 0 ? pageSize : 10;
        int safeOffset = Math.max(0, offset);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        List<CampaignStatus> statuses = resolveRiskImpactStatuses(status);

        if (riskImpact != null) {
            List<Campaign> allMatching = campaignRepository.findWithStatuses(
                    clientId,
                    searchParam,
                    statuses,
                    effectiveChannel,
                    type,
                    startDate,
                    endDate,
                    true,
                    Pageable.unpaged(sort)).getContent();

            List<CampaignRiskImpactDto> filtered = allMatching.stream()
                    .map(this::buildCampaignRiskImpact)
                    .filter(dto -> riskImpact == dto.getRiskImpact())
                    .toList();

            int fromIndex = Math.min(safeOffset * safePageSize, filtered.size());
            int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
            List<CampaignRiskImpactDto> pageItems = filtered.subList(fromIndex, toIndex);
            return new PageImpl<>(pageItems, PageRequest.of(safeOffset, safePageSize, sort), filtered.size());
        }

        Pageable pageable = PageRequest.of(safeOffset, safePageSize, sort);
        Page<Campaign> campaigns = campaignRepository.findWithStatuses(
                clientId,
                searchParam,
                statuses,
                effectiveChannel,
                type,
                startDate,
                endDate,
                true,
                pageable);

        List<CampaignRiskImpactDto> items = campaigns.getContent().stream()
                .map(this::buildCampaignRiskImpact)
                .toList();
        return new PageImpl<>(items, pageable, campaigns.getTotalElements());
    }

    @Override
    public UserCampaignStatisticsDto getUserCampaignStatistics(CampaignChannel channel) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UserType userType = parseUserType(context);
        if (userType != UserType.USER) {
            throw new ServiceException("Campaign user statistics are only available for USER accounts");
        }

        String userId = context.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ServiceException("User id is required");
        }

        String trimmedUserId = userId.trim();
        String clientId = context.getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);
        if (campaignIds == null || campaignIds.isEmpty()) {
            return emptyUserCampaignStatistics();
        }

        long totalCampaigns = recipientRepository.countByClientIdAndUserIdAndCampaignIdIn(
                clientId, trimmedUserId, campaignIds);
        if (effectiveChannel == CampaignChannel.VOICE) {
            return UserCampaignStatisticsDto.builder()
                    .totalCampaigns(totalCampaigns)
                    .openCount(countUserRecipientsByStatuses(clientId, trimmedUserId, campaignIds,
                            List.of(RecipientStatus.ANSWERED, RecipientStatus.VOICE_ENGAGED,
                                    RecipientStatus.COMPROMISED)))
                    .clickCount(countUserRecipientsByStatuses(clientId, trimmedUserId, campaignIds,
                            List.of(RecipientStatus.VOICE_ENGAGED, RecipientStatus.COMPROMISED)))
                    .compromiseCount(countUserRecipientsByStatus(
                            clientId, trimmedUserId, campaignIds, RecipientStatus.COMPROMISED))
                    .reportCount(countUserRecipientsByStatus(
                            clientId, trimmedUserId, campaignIds, RecipientStatus.REPORTED))
                    .build();
        }

        return UserCampaignStatisticsDto.builder()
                .totalCampaigns(totalCampaigns)
                .openCount(countUserRecipientsByStatuses(clientId, trimmedUserId, campaignIds,
                        List.of(RecipientStatus.OPENED, RecipientStatus.CLICKED,
                                RecipientStatus.DATA_SUBMITTED, RecipientStatus.REPORTED)))
                .clickCount(countUserRecipientsByStatuses(clientId, trimmedUserId, campaignIds,
                        List.of(RecipientStatus.CLICKED, RecipientStatus.DATA_SUBMITTED)))
                .compromiseCount(countUserRecipientsByStatus(
                        clientId, trimmedUserId, campaignIds, RecipientStatus.DATA_SUBMITTED))
                .reportCount(countUserRecipientsByStatus(
                        clientId, trimmedUserId, campaignIds, RecipientStatus.REPORTED))
                .build();
    }

    @Override
    public EndUserPhishingVisibilityDto hasReceivedPhishingCampaign() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UserType userType = parseUserType(context);
        if (userType != UserType.USER) {
            throw new ServiceException("Phishing campaign visibility is only available for USER accounts");
        }

        String userId = context.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ServiceException("User id is required");
        }

        String trimmedUserId = userId.trim();
        String clientId = context.getClientAdminId();
        List<CampaignRecipient> recipients = recipientRepository.findByClientIdAndUserId(clientId, trimmedUserId);
        if (recipients == null || recipients.isEmpty()) {
            return EndUserPhishingVisibilityDto.builder()
                    .hasReceivedCampaign(false)
                    .hasReceivedEmailCampaign(false)
                    .hasReceivedSmsCampaign(false)
                    .hasReceivedVoiceCampaign(false)
                    .build();
        }

        List<String> campaignIds = recipients.stream()
                .map(CampaignRecipient::getCampaignId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Set<CampaignChannel> channels = new HashSet<>();
        if (!campaignIds.isEmpty()) {
            for (Campaign campaign : campaignRepository.findByIdInAndClientId(campaignIds, clientId)) {
                channels.add(campaign.getChannel() != null ? campaign.getChannel() : CampaignChannel.EMAIL);
            }
        }

        return EndUserPhishingVisibilityDto.builder()
                .hasReceivedCampaign(true)
                .hasReceivedEmailCampaign(channels.contains(CampaignChannel.EMAIL))
                .hasReceivedSmsCampaign(channels.contains(CampaignChannel.SMS))
                .hasReceivedVoiceCampaign(channels.contains(CampaignChannel.VOICE))
                .build();
    }

    @Override
    public boolean isFirstCampaign(String productPackageId, String campaignId) {
        if (!StringUtils.hasText(productPackageId)) {
            throw new PhishingValidationException("productPackageId is required");
        }
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String packageId = productPackageId.trim();
        if (StringUtils.hasText(campaignId)) {
            return !campaignRepository.existsByClientIdAndProductPackageIdAndIdNot(
                    clientId, packageId, campaignId.trim());
        }
        return !campaignRepository.existsByClientIdAndProductPackageId(clientId, packageId);
    }

    @Override
    public CampaignStatsDto refreshCampaignStats(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        // Recalculate stats from recipients
        CampaignStats stats = campaign.getStats();
        if (stats == null) {
            stats = new CampaignStats();
        }
        
        stats.setTotalRecipients((int) recipientRepository.countByCampaignId(campaignId));
        if (campaign.isVoiceChannel()) {
            stats.setCallsAnswered((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.ANSWERED, RecipientStatus.VOICE_ENGAGED, RecipientStatus.COMPROMISED)));
            stats.setCallsEngaged((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.VOICE_ENGAGED, RecipientStatus.COMPROMISED)));
            stats.setCallsCompromised((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.COMPROMISED));
            stats.setCallsNoAnswer((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.NO_ANSWER));
            stats.setCallsFailed((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.CALL_FAILED));
        } else {
            stats.setEmailsSent((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.SENT, RecipientStatus.DELIVERED, RecipientStatus.OPENED,
                            RecipientStatus.CLICKED, RecipientStatus.DATA_SUBMITTED, RecipientStatus.REPORTED)));
            stats.setEmailsDelivered((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.DELIVERED, RecipientStatus.OPENED,
                            RecipientStatus.CLICKED, RecipientStatus.DATA_SUBMITTED, RecipientStatus.REPORTED)));
            stats.setEmailsOpened((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.OPENED, RecipientStatus.CLICKED,
                            RecipientStatus.DATA_SUBMITTED, RecipientStatus.REPORTED)));
            stats.setLinksClicked((int) recipientRepository.countByCampaignIdAndStatusIn(campaignId,
                    List.of(RecipientStatus.CLICKED, RecipientStatus.DATA_SUBMITTED)));
            stats.setDataSubmitted((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.DATA_SUBMITTED));
            stats.setEmailsReported((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.REPORTED));
            stats.setEmailsBounced((int) recipientRepository.countByCampaignIdAndStatus(
                    campaignId, RecipientStatus.BOUNCED));
        }
        stats.setLastUpdatedAt(Instant.now());
        
        campaign.setStats(stats);
        campaignRepository.save(campaign);
        
        return campaignMapper.toStatsDto(stats);
    }

    /**
     * Default risk-impact listing excludes DRAFT (no simulated activity yet).
     * An explicit status filter is honored as-is, including DRAFT.
     */
    private static List<CampaignStatus> resolveRiskImpactStatuses(CampaignStatus status) {
        if (status != null) {
            return List.of(status);
        }
        return List.of(
                CampaignStatus.SCHEDULED,
                CampaignStatus.RUNNING,
                CampaignStatus.PAUSED,
                CampaignStatus.COMPLETED,
                CampaignStatus.EXPIRED,
                CampaignStatus.CANCELLED);
    }

    private CampaignRiskImpactDto buildCampaignRiskImpact(Campaign campaign) {
        List<CampaignRecipient> recipients = recipientRepository.findByCampaignId(campaign.getId());
        Map<RiskLevel, Integer> riskCounts = new EnumMap<>(RiskLevel.class);
        if (!recipients.isEmpty()) {
            List<String> userIds = recipients.stream()
                    .map(CampaignRecipient::getUserId)
                    .filter(Objects::nonNull)
                    .filter(id -> !id.isBlank())
                    .distinct()
                    .toList();
            if (!userIds.isEmpty()) {
                userRiskProfileRepository.findByClientIdAndUserIdIn(campaign.getClientId(), userIds)
                        .forEach(profile -> {
                            RiskLevel riskLevel = profile.getRiskLevel();
                            if (riskLevel != null) {
                                riskCounts.merge(riskLevel, 1, Integer::sum);
                            }
                        });
            }
        }

        RiskLevel topRiskImpact = riskCounts.entrySet().stream()
                .max(Comparator.<Map.Entry<RiskLevel, Integer>>comparingInt(Map.Entry::getValue)
                        .thenComparing(entry -> getRiskPriority(entry.getKey())))
                .map(Map.Entry::getKey)
                .orElse(null);

        int impactedUsers = topRiskImpact != null ? riskCounts.getOrDefault(topRiskImpact, 0) : 0;

        return CampaignRiskImpactDto.builder()
                .campaignId(emptyIfNull(campaign.getId()))
                .campaignName(emptyIfNull(campaign.getCampaignName()))
                .type(campaign.getCampaignType() != null ? campaign.getCampaignType().name() : "")
                .targetGroup(resolveTargetGroup(campaign))
                .riskImpact(topRiskImpact)
                .aiRating(resolveAiRating(topRiskImpact))
                .impactedUserCount(impactedUsers)
                .startDate(campaign.getCreatedAt())
                .endDate(campaign.getExpiresAt())
                .status(campaign.getStatus().name())
                .build();
    }

    private String resolveTargetGroup(Campaign campaign) {
        if (campaign == null || campaign.getAudience() == null || campaign.getAudience().getType() == null) {
            return "";
        }
        return campaign.getAudience().getType().name();
    }

    private String resolveAiRating(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return "";
        }
        return switch (riskLevel) {
            case CRITICAL -> "Too Effective";
            case HIGH -> "Effective";
            case MEDIUM -> "Balanced";
            case LOW -> "Ineffective";
        };
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private int getRiskPriority(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return 0;
        }
        return switch (riskLevel) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    // --- Helper Methods ---

    private void applyAssignedForFromRequest(Campaign campaign, CampaignCreateRequest request) {
        CampaignChannel channel = CampaignTypeChannelValidator.effectiveChannel(request.getChannel());
        if (channel == CampaignChannel.VOICE) {
            campaign.setResponseStages(request.getResponseStages() != null
                    ? new ArrayList<>(request.getResponseStages()) : new ArrayList<>());
            campaign.setLearningMode(request.getLearningMode());
            SubPackageAssignedFor mapped = VoiceResponseStageMapper.toAssignedFor(campaign.getResponseStages());
            campaign.setAssignedFor(mapped);
        } else {
            campaign.setAssignedFor(request.getAssignedFor());
        }
    }

    private void applyLicenseScopeFromRequest(Campaign campaign, CampaignCreateRequest request) {
        if (request.getProductPackageId() != null && !request.getProductPackageId().isBlank()) {
            campaign.setProductPackageId(request.getProductPackageId().trim());
        }
    }

    private void copyLicenseScopeFromTrainingData(Campaign campaign) {
        CampaignTrainingData trainingData = campaign.getTrainingData();
        if (trainingData == null) {
            return;
        }
        if (trainingData.getProductPackageId() != null && !trainingData.getProductPackageId().isBlank()) {
            campaign.setProductPackageId(trainingData.getProductPackageId().trim());
        }
    }

    /**
     * Ensures productPackageId is present before launch so unique-user license
     * accounting can attribute recipients to a ClientProduct assignment.
     */
    private void ensureLicenseScopeForLaunch(Campaign campaign) {
        copyLicenseScopeFromTrainingData(campaign);
        if (campaign.getProductPackageId() == null || campaign.getProductPackageId().isBlank()) {
            throw new PhishingValidationException(
                    "productPackageId is required before launch so campaign recipients "
                            + "can be counted against the correct product-package license");
        }
    }

    private void validateDeliveryConfiguration(Campaign campaign, String clientId) {
        CampaignChannel channel = campaign.getChannel() != null ? campaign.getChannel() : CampaignChannel.EMAIL;
        if (channel == CampaignChannel.VOICE) {
            if (campaign.getVoiceData() == null || campaign.getVoiceData().getVoiceCloneId() == null) {
                throw new PhishingValidationException("Voice clone is required");
            }
            if (campaign.getVoiceScenario() == null || campaign.getVoiceScenario().getScenarioId() == null) {
                throw new PhishingValidationException("Vishing scenario is required");
            }
            String serverId = campaign.getTelephonyData() != null
                    ? campaign.getTelephonyData().getVoiceServerConfigurationId()
                    : campaign.getVoiceServerConfigurationId();
            VoiceServerConfiguration voiceServer = voiceServerConfigurationService.resolveForCampaign(clientId, serverId);
            String callerId = campaign.getVoiceData().getCallerId() != null
                    ? campaign.getVoiceData().getCallerId()
                    : voiceServer.getCallerId();
            if (!StringUtils.hasText(callerId)) {
                throw new PhishingValidationException("Caller ID is required for voice campaigns");
            }
            List<CampaignRecipient> recipients = recipientRepository.findByCampaignId(campaign.getId());
            recipientResolverFactory.getResolver(CampaignChannel.VOICE).validateBeforeLaunch(campaign, recipients);
        } else if (channel == CampaignChannel.SMS) {
            if (campaign.getEmailTemplateId() == null) {
                throw new PhishingValidationException("SMS template is required");
            }
            EmailTemplate template = emailTemplateRepository.findById(campaign.getEmailTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("SMS template not found"));
            if (template.getTemplateType() != TemplateType.SMS) {
                throw new PhishingValidationException("Selected template is not an SMS template");
            }
            smsServerConfigurationService.resolveForCampaign(clientId, campaign.getSmsServerConfigurationId());
            CampaignRecipient sample = recipientRepository.findByCampaignId(campaign.getId()).stream().findFirst()
                    .orElseThrow(() -> new PhishingValidationException("No recipients for SMS length validation"));
            LandingPage landingPage = campaign.getLandingPageId() != null
                    ? landingPageRepository.findById(campaign.getLandingPageId()).orElse(null) : null;
            String landingDomainId = landingPage != null ? landingPage.getTrackingDomainId() : null;
            String shortOrigin = trackingBaseUrlResolver.resolveShortLinkOrigin(
                            campaign.getClientId(), campaign.getTrackingDomainId(), landingDomainId)
                    .orElseThrow(() -> new PhishingValidationException(
                            "A verified tracking domain is required for SMS campaigns"));
            String smsUrl = urlShortenerService.previewUrl(shortOrigin);
            smsTemplateValidator.validateRenderedLength(template.getSmsBody(), smsUrl, sample);
        } else {
            if (campaign.getSenderProfileId() == null) {
                throw new PhishingValidationException("Sender profile is required for email campaigns");
            }
            if (!senderProfileRepository.findByIdAndClientIdOrGlobal(campaign.getSenderProfileId(), clientId).isPresent()) {
                throw new ResourceNotFoundException("Sender profile not found");
            }
        }
    }

    private CampaignDto toCampaignDtoWithNames(Campaign campaign) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignDto dto = campaignMapper.toDto(campaign);
        enrichSingleCampaignDtoNames(dto, clientId);
        enrichCampaignTrainingTopicDetailsBatch(List.of(dto));
        return dto;
    }

    /**
     * For SMS campaigns, mirrors gateway id/name into sender profile fields so step-4 UI can reuse email fields.
     */
    private void applySmsGatewayStep4Alias(CampaignDto dto, String gatewayId, String gatewayName) {
        if (dto == null || dto.getChannel() != CampaignChannel.SMS) {
            return;
        }
        if (gatewayId == null || gatewayId.isBlank()) {
            return;
        }
        String trimmedId = gatewayId.trim();
        dto.setSmsServerConfigurationId(trimmedId);
        dto.setSmsServerConfigurationName(gatewayName);
        dto.setSenderProfileId(trimmedId);
        dto.setSenderProfileName(gatewayName);
    }

    /**
     * Fills email template, landing page, and sender profile display names from Mongo.
     * Template/sender use client-or-global visibility; landing page resolves directly by id.
     */
    private void enrichSingleCampaignDtoNames(CampaignDto dto, String clientId) {
        if (dto == null) {
            return;
        }
        if (dto.getEmailTemplateId() != null && !dto.getEmailTemplateId().isBlank()) {
            emailTemplateRepository.findByIdAndClientIdOrGlobal(dto.getEmailTemplateId().trim(), clientId)
                    .ifPresent(t -> dto.setEmailTemplateName(t.getTemplateName()));
        }
        if (dto.getLandingPageId() != null && !dto.getLandingPageId().isBlank()) {
            landingPageRepository.findById(dto.getLandingPageId().trim())
                    .ifPresent(lp -> {
                        dto.setLandingPageName(lp.getName());
                        enrichCampaignTrackingDomain(dto, lp);
                    });
        } else {
            enrichCampaignTrackingDomain(dto, null);
        }
        if (dto.getChannel() != CampaignChannel.SMS
                && dto.getSenderProfileId() != null && !dto.getSenderProfileId().isBlank()) {
            senderProfileRepository.findByIdAndClientIdOrGlobal(dto.getSenderProfileId().trim(), clientId)
                    .ifPresent(p -> dto.setSenderProfileName(p.getProfileName()));
        }
        if (dto.getSmsServerConfigurationId() != null && !dto.getSmsServerConfigurationId().isBlank()) {
            String smsConfigId = dto.getSmsServerConfigurationId().trim();
            smsServerConfigurationRepository.findByIdAndClientId(smsConfigId, clientId)
                    .ifPresentOrElse(
                            s -> applySmsGatewayStep4Alias(dto, s.getId(), s.getName()),
                            () -> applySmsGatewayStep4Alias(dto, smsConfigId, null));
        }
        if (dto.getVoiceServerConfigurationId() != null && !dto.getVoiceServerConfigurationId().isBlank()) {
            voiceServerConfigurationRepository.findByIdAndClientId(dto.getVoiceServerConfigurationId().trim(), clientId)
                    .ifPresent(v -> dto.setVoiceServerConfigurationName(v.getName()));
        }
        if (dto.getVoiceScenario() != null && dto.getVoiceScenario().getScenarioId() != null) {
            vishingScenarioRepository.findByIdAndClientIdOrGlobal(dto.getVoiceScenario().getScenarioId(), clientId)
                    .ifPresent(s -> dto.getVoiceScenario().setScenarioName(s.getScenarioName()));
        }
    }

    /**
     * Batch-resolves template and sender names for list APIs (two loads by id set, not N+1 per row).
     */
    private void enrichCampaignDtosBatch(List<CampaignDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        Set<String> templateIds = dtos.stream()
                .map(CampaignDto::getEmailTemplateId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> senderIds = dtos.stream()
                .map(CampaignDto::getSenderProfileId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> landingPageIds = dtos.stream()
                .map(CampaignDto::getLandingPageId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> smsServerIds = dtos.stream()
                .filter(d -> d.getChannel() == CampaignChannel.SMS)
                .map(CampaignDto::getSmsServerConfigurationId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Map<String, String> templateNames = new HashMap<>();
        if (!templateIds.isEmpty()) {
            emailTemplateRepository.findAllById(templateIds).forEach(t -> {
                if (t.getId() != null && t.getTemplateName() != null) {
                    templateNames.put(t.getId(), t.getTemplateName());
                }
            });
        }
        Map<String, String> senderNames = new HashMap<>();
        if (!senderIds.isEmpty()) {
            senderProfileRepository.findAllById(senderIds).forEach(p -> {
                if (p.getId() != null && p.getProfileName() != null) {
                    senderNames.put(p.getId(), p.getProfileName());
                }
            });
        }
        Map<String, LandingPage> landingPagesById = new HashMap<>();
        if (!landingPageIds.isEmpty()) {
            landingPageRepository.findAllById(landingPageIds).forEach(lp -> {
                if (lp.getId() != null) {
                    landingPagesById.put(lp.getId(), lp);
                }
            });
        }
        Map<String, String> smsServerNames = new HashMap<>();
        if (!smsServerIds.isEmpty()) {
            smsServerConfigurationRepository.findAllById(smsServerIds).forEach(s -> {
                if (s.getId() != null && s.getName() != null) {
                    smsServerNames.put(s.getId(), s.getName());
                }
            });
        }
        for (CampaignDto dto : dtos) {
            if (dto.getEmailTemplateId() != null && !dto.getEmailTemplateId().isBlank()) {
                String tid = dto.getEmailTemplateId().trim();
                dto.setEmailTemplateName(templateNames.get(tid));
            }
            if (dto.getChannel() != CampaignChannel.SMS
                    && dto.getSenderProfileId() != null && !dto.getSenderProfileId().isBlank()) {
                String sid = dto.getSenderProfileId().trim();
                dto.setSenderProfileName(senderNames.get(sid));
            }
            if (dto.getChannel() == CampaignChannel.SMS
                    && dto.getSmsServerConfigurationId() != null && !dto.getSmsServerConfigurationId().isBlank()) {
                String smsConfigId = dto.getSmsServerConfigurationId().trim();
                applySmsGatewayStep4Alias(dto, smsConfigId, smsServerNames.get(smsConfigId));
            }
            if (dto.getLandingPageId() != null && !dto.getLandingPageId().isBlank()) {
                LandingPage landingPage = landingPagesById.get(dto.getLandingPageId().trim());
                enrichCampaignTrackingDomain(dto, landingPage);
            } else {
                enrichCampaignTrackingDomain(dto, null);
            }
        }
        enrichCampaignTrainingTopicDetailsBatch(dtos);
    }

    private void enrichCampaignTrackingDomain(CampaignDto dto, LandingPage landingPage) {
        if (dto == null) {
            return;
        }
        String effectiveDomainId = StringUtils.hasText(dto.getTrackingDomainId())
                ? dto.getTrackingDomainId().trim()
                : (landingPage != null && StringUtils.hasText(landingPage.getTrackingDomainId())
                        ? landingPage.getTrackingDomainId().trim() : null);
        dto.setTrackingDomainId(effectiveDomainId);
    }

    /**
     * Fills {@code trainingData.topicIdDetails} from CMS without mutating the persisted {@link CampaignTrainingData}
     * instance attached to the entity (mapper reuses that reference on the DTO).
     */
    private void enrichCampaignTrainingTopicDetailsBatch(List<CampaignDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        Set<String> uniqueTopicIds = new HashSet<>();
        for (CampaignDto dto : dtos) {
            CampaignTrainingData td = dto.getTrainingData();
            if (td == null || td.getTopicId() == null) {
                continue;
            }
            for (String rawId : td.getTopicId()) {
                if (rawId != null && !rawId.isBlank()) {
                    uniqueTopicIds.add(rawId.trim());
                }
            }
        }
        Map<String, String> topicNames = uniqueTopicIds.isEmpty()
                ? Collections.emptyMap()
                : resolveTopicNamesById(uniqueTopicIds);
        for (CampaignDto dto : dtos) {
            CampaignTrainingData td = dto.getTrainingData();
            if (td == null) {
                continue;
            }
            List<String> topicIds = td.getTopicId();
            if (topicIds == null || topicIds.isEmpty()) {
                continue;
            }
            dto.setTrainingData(copyTrainingDataWithTopicDetails(td, buildTopicIdDetails(topicIds, topicNames)));
        }
    }

    private Map<String, String> resolveTopicNamesById(Set<String> ids) {
        Map<String, String> out = new HashMap<>();
        for (String id : ids) {
            cmsSubPackageClient.fetchTopicById(id).ifPresent(d -> out.put(id, d.getTopicName()));
        }
        return out;
    }

    private static List<TopicIdDetailDto> buildTopicIdDetails(List<String> topicIds, Map<String, String> topicNames) {
        List<TopicIdDetailDto> details = new ArrayList<>(topicIds.size());
        for (String rawId : topicIds) {
            if (rawId == null || rawId.isBlank()) {
                details.add(TopicIdDetailDto.builder().tid(rawId).topicName(null).build());
            } else {
                String id = rawId.trim();
                details.add(TopicIdDetailDto.builder().tid(id).topicName(topicNames.get(id)).build());
            }
        }
        return details;
    }

    private static CampaignTrainingData copyTrainingDataWithTopicDetails(
            CampaignTrainingData src, List<TopicIdDetailDto> topicIdDetails) {
        return CampaignTrainingData.builder()
                .trainingModuleId(src.getTrainingModuleId())
                .name(src.getName())
                .description(src.getDescription())
                .productId(src.getProductId())
                .packageId(src.getPackageId())
                .productPackageId(src.getProductPackageId())
                .clientId(src.getClientId())
                .topicId(src.getTopicId() != null ? new ArrayList<>(src.getTopicId()) : null)
                .assignedFor(src.getAssignedFor())
                .completionDays(src.getCompletionDays())
                .topicIdDetails(topicIdDetails)
                .build();
    }

    private static CampaignTrainingData.CompletionDays toStoredCompletionDays(
            CampaignTrainingRequest.CompletionDays completionDays) {
        if (completionDays == null) {
            return null;
        }
        String unit = completionDays.getDurationUnit() == null ? null
                : completionDays.getDurationUnit().trim().toUpperCase(Locale.ROOT);
        return CampaignTrainingData.CompletionDays.builder()
                .durationUnit(unit)
                .durationValue(completionDays.getDurationValue())
                .build();
    }

    private Campaign getCampaignForEdit(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        if (!campaign.isEditable()) {
            throw new PhishingValidationException("Cannot modify campaign. Only draft campaigns can be edited.");
        }
        
        return campaign;
    }

    private void updateStepProgress(Campaign campaign, int completedStep) {
        if (campaign.getCurrentStep() < completedStep + 1) {
            campaign.setCurrentStep(completedStep + 1);
        }
    }

    private void validateExpiryAgainstSchedule(Instant expiresAt, CampaignSchedule schedule) {
        if (expiresAt == null || schedule == null || schedule.getStartDateTime() == null) {
            return;
        }
        if (!expiresAt.isAfter(schedule.getStartDateTime())) {
            throw new PhishingValidationException("Campaign expiration must be after schedule start time");
        }
    }

    private Instant computeExpiresAtFromRequest(Campaign campaign, CampaignCreateRequest request) {
        if (request == null || request.getExpireDate() == null
                || request.getExpireDate().getValidityUnit() == null
                || request.getExpireDate().getValidityPeriod() == null) {
            throw new PhishingValidationException("Expire date is required");
        }
        int validityPeriod = request.getExpireDate().getValidityPeriod();
        if (validityPeriod <= 0) {
            throw new PhishingValidationException("Expire validity period must be greater than 0");
        }

        Instant baseInstant = resolveExpireBaseInstant(campaign);
        try {
            LocalDateTime baseDateTime = LocalDateTime.ofInstant(baseInstant, ZoneOffset.UTC);
            LocalDateTime expiresDateTime = switch (request.getExpireDate().getValidityUnit()) {
                case DAYS -> baseDateTime.plusDays(validityPeriod);
                case MONTHS -> baseDateTime.plusMonths(validityPeriod);
            };
            return expiresDateTime.toInstant(ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new PhishingValidationException("Invalid expire date configuration");
        }
    }

    private Instant resolveExpireBaseInstant(Campaign campaign) {
        if (campaign != null
                && campaign.getSchedule() != null
                && campaign.getSchedule().getStartDateTime() != null) {
            return campaign.getSchedule().getStartDateTime();
        }
        return Instant.now();
    }

    private int calculateAndCreateRecipients(Campaign campaign, CampaignAudienceRequest request, String clientId) {
        // Delete existing recipients
        recipientRepository.deleteByCampaignId(campaign.getId());

        List<RegistrationServiceClient.UserDto> users = campaignAudienceUserResolver.resolveLicensed(
                clientId, campaign.getProductPackageId(), request);

        // For users where isRiskProfileExist is false: save risk profile with full user fields and collect userIds to sync back to registration
        List<String> userIdsToUpdateRiskProfileExist = new ArrayList<>();
        for (RegistrationServiceClient.UserDto user : users) {
            if (Boolean.FALSE.equals(user.getIsRiskProfileExist()) && user.getUserId() != null) {
                saveRiskProfileFromCampaignUser(user, clientId);
                userIdsToUpdateRiskProfileExist.add(user.getUserId());
            } else {
                campaignUserRiskProfileAsyncUpdater.enablePhishingForExistingProfile(user.getUserId(), clientId);
            }
        }

        if (!userIdsToUpdateRiskProfileExist.isEmpty()) {
            registrationClient.updateRiskProfileExist(userIdsToUpdateRiskProfileExist, true);
        }

        // Create recipient records
        List<RegistrationServiceClient.UserDto> eligibleUsers =
                recipientResolverFactory.getResolver(campaign.getChannel()).filterEligibleUsers(users);

        List<CampaignRecipient> recipients = eligibleUsers.stream()
                .map(user -> CampaignRecipient.builder()
                        .campaignId(campaign.getId())
                        .campaignName(campaign.getCampaignName())
                        .clientId(clientId)
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .department(user.getDepartmentName())
                        .organizationName(user.getOrganizationName())
                        .organizationDomain(user.getOrganizationDomain())
                        .phoneNumber(user.getPhoneNumber())
                        .countryName(user.getCountryName())
                        .status(RecipientStatus.PENDING)
                        .trackingId(generateTrackingId())
                        .build())
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            recipientRepository.saveAll(recipients);
        }

        return recipients.size();
    }

    /**
     * Saves or updates a UserRiskProfile from campaign audience user data, passing all user fields
     * (email, firstName, lastName, departmentId, departmentName, groupIds, active, isRiskProfileExist)
     * so the risk profile is kept in sync with registration user data.
     */
    private void saveRiskProfileFromCampaignUser(RegistrationServiceClient.UserDto user, String clientId) {
        UserRiskProfileSaveRequestDto request = UserRiskProfileSaveRequestDto.builder()
                .clientId(clientId)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .department(user.getDepartmentName())
                .isPhishingEnabled(true)
                .fromPhishingContext(true)
                .build();
        userRiskProfileService.save(request);
    }

    private String generateTrackingId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // --- Topic Recommendation ---

    @Override
    public CmsTopicFilterResponseDto getRecommendedTopics(String campaignId, String productId,
                                                           String packageId, Integer page, Integer size) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        // Load related entities if set on campaign
        EmailTemplate emailTemplate = null;
        if (campaign.getEmailTemplateId() != null) {
            emailTemplate = emailTemplateRepository.findById(campaign.getEmailTemplateId()).orElse(null);
        }

        LandingPage landingPage = null;
        if (campaign.getLandingPageId() != null) {
            landingPage = landingPageRepository.findById(campaign.getLandingPageId()).orElse(null);
        }

        SenderProfile senderProfile = null;
        if (campaign.getSenderProfileId() != null) {
            senderProfile = senderProfileRepository.findById(campaign.getSenderProfileId()).orElse(null);
        }

        // Fetch client admin context from registration service
        ClientAdminInfoDto adminInfo = registrationClient.getClientAdminInfo(clientId);

        // Build filter request
        int cmsPage = (page != null && page > 0) ? page : 1;
        int cmsSize = (size != null && size > 0) ? size : 20;
        CmsTopicFilterRequestDto filterRequest = topicRecommendationFilterAssembler.build(
                campaign, emailTemplate, landingPage, senderProfile, adminInfo, cmsPage, cmsSize);
        filterRequest.setClientId(clientId);

        String resolvedProductId = resolveProductId(campaign, productId);
        String resolvedPackageId = resolvePackageId(campaign, packageId);

        if (resolvedProductId != null) {
            log.info("Calling CMS recommendTopicsByProduct. campaignId={}, clientId={}, productId={}, ignoredPackageId={}, page={}, size={}, filter={}",
                    campaignId, clientId, resolvedProductId, resolvedPackageId, cmsPage, cmsSize, filterRequest);
            return cmsTopicClient.recommendTopicsByProduct(resolvedProductId, filterRequest);
        }
        if (resolvedPackageId != null) {
            log.info("Calling CMS recommendTopicsByPackage. campaignId={}, clientId={}, packageId={}, page={}, size={}, filter={}",
                    campaignId, clientId, resolvedPackageId, cmsPage, cmsSize, filterRequest);
            return cmsTopicClient.recommendTopicsByPackage(resolvedPackageId, filterRequest);
        }
        log.info("Calling CMS recommendTopics (unscoped). campaignId={}, clientId={}, page={}, size={}, filter={}",
                campaignId, clientId, cmsPage, cmsSize, filterRequest);
        return cmsTopicClient.recommendTopics(filterRequest);
    }

    private static String resolveProductId(Campaign campaign, String queryProductId) {
        if (queryProductId != null && !queryProductId.isBlank()) {
            return queryProductId.trim();
        }
        if (campaign.getTrainingData() != null
                && campaign.getTrainingData().getProductId() != null
                && !campaign.getTrainingData().getProductId().isBlank()) {
            return campaign.getTrainingData().getProductId().trim();
        }
        return null;
    }

    private static String resolvePackageId(Campaign campaign, String queryPackageId) {
        if (queryPackageId != null && !queryPackageId.isBlank()) {
            return queryPackageId.trim();
        }
        if (campaign.getTrainingData() != null
                && campaign.getTrainingData().getPackageId() != null
                && !campaign.getTrainingData().getPackageId().isBlank()) {
            return campaign.getTrainingData().getPackageId().trim();
        }
        return null;
    }

    private UserType parseUserType(CurrentUserContext context) {
        if (context == null || context.getUserType() == null) {
            return null;
        }
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private static UserCampaignStatisticsDto emptyUserCampaignStatistics() {
        return UserCampaignStatisticsDto.builder()
                .totalCampaigns(0L)
                .openCount(0L)
                .clickCount(0L)
                .compromiseCount(0L)
                .reportCount(0L)
                .build();
    }

    private long countUserRecipientsByStatuses(
            String clientId, String userId, List<String> campaignIds, List<RecipientStatus> statuses) {
        return recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatusIn(
                clientId, userId, campaignIds, statuses);
    }

    private long countUserRecipientsByStatus(
            String clientId, String userId, List<String> campaignIds, RecipientStatus status) {
        return recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatus(
                clientId, userId, campaignIds, status);
    }

}
