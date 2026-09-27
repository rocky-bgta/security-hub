package com.aspire.asat.cms.service.certificate;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.exam.CertificateLinksDTO;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.CertificateTemplateRepository;
import com.aspire.asat.cms.repository.ClientCertificateTemplateRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.UserCertificateRepositoryCustom;
import com.aspire.asat.cms.model.CertificateTemplate;
import com.aspire.asat.cms.model.ClientCertificateTemplate;
import com.aspire.asat.cms.util.CertificateGenerator;
import com.aspire.asat.cms.util.CertificateStatusUtil;
import com.aspire.asat.cms.util.CommonUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final CertificateGenerator certificateGenerator;
    private final FileService fileService;
    private final UserSubPackageRepository userSubPackageRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final UserCertificateRepositoryCustom userCertificateRepositoryCustom;
    private final SubPackageRepository subPackageRepository;
    private final ProductRepository productRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final CertificateTemplateRepository certificateTemplateRepository;
    private final ClientCertificateTemplateRepository clientCertificateTemplateRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ClientAdminServiceClient clientAdminServiceClient;

    @Value("${service.registration.url}")
    private String registrationUrl;

    private static final int EXPIRING_SOON_DAYS = 30;

    @Override
    public List<CertificateResponseDTO> getUserCertificates(String userId, String packageId, int offset, int pageSize) {
        log.debug("Fetching certificates for userId={}, packageId={}, offset={}, pageSize={}", userId, packageId, offset, pageSize);

        try {
            offset = CommonUtil.getOffset(offset, pageSize);

            List<UserSubPackage> userPackages;
            if (packageId != null && !packageId.isBlank()) {
                userPackages = userSubPackageRepository.findByUserIdAndSubPackageIdAndStatusAndCertificateLinkIsNotNull(userId, packageId, SubPackageStatus.COMPLETED.name());
            } else {
                userPackages = userSubPackageRepository.findByUserIdAndStatusAndCertificateLinkIsNotNull(userId, SubPackageStatus.COMPLETED.name(),
                        PageRequest.of(offset / pageSize, pageSize));
            }

            return userPackages.stream()
                    .map(userPackage -> {
                        ProductInfo productInfo = fetchProductInfoByPackageId(userPackage.getSubPackageId());
                        return CertificateResponseDTO.builder()
                                .packageId(userPackage.getSubPackageId())
                                .packageName(userPackage.getSubPackageName())
                                .thumbnailUrl(null)
                                .certificateLink(userPackage.getCertificateLink())
                                .imageCertificateLink(userPackage.getImageCertificateLink())
                                .createdAt(userPackage.getLastSynced())
                                .productId(productInfo.productId)
                                .productName(productInfo.productName)
                                .thumbnailUrl(productInfo.thumbnailUrl)
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch user certificates for userId={}, packageId={}. Error: {}", userId, packageId, e.getMessage(), e);
            return List.of();
        }
    }


    private record ProductInfo(String productId, String productName, String thumbnailUrl) {
    }

    private ProductInfo fetchProductInfoByPackageId(String subPackageId) {
        SubPackage subPackage = subPackageRepository.findById(subPackageId).orElseThrow(
                () -> new ResourceNotFoundException("Invalid subPackageId: " + subPackageId));

        return productRepository.findById(subPackage.getProductId()).map(product -> new ProductInfo(product.getId(), product.getProductName(), product.getThumbnailUrl()))
                .orElse(new ProductInfo(null, null, null));

    }

    @Override
    public long countUserCertificates(String userId, String packageId) {
        log.debug("Counting certificates for userId={}, packageId={}", userId, packageId);
        try {
            long count;
            if (packageId != null && !packageId.isBlank()) {
                count = userSubPackageRepository.findByUserIdAndSubPackageIdAndStatusAndCertificateLinkIsNotNull(userId, packageId, SubPackageStatus.COMPLETED.name()).size();
            } else {
                count = userSubPackageRepository.findByUserIdAndStatusAndCertificateLinkIsNotNull(userId, SubPackageStatus.COMPLETED.name(),
                        org.springframework.data.domain.Pageable.unpaged()).size();
            }
            log.debug("Total certificates found: {} for userId={}, packageId={}", count, userId, packageId);
            return count;
        } catch (Exception e) {
            log.error("Error counting certificates for userId={}, packageId={}: {}", userId, packageId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public CertificateStatsResponseDTO getCertificateStats(String userId) {
        List<UserCertificate> userCertificates = userCertificateRepository.findByUserId(userId);
        Instant now = Instant.now();

        int total = userCertificates.size();
        int valid = 0;
        int expired = 0;
        int expiringSoon = 0;

        Instant expiringSoonThreshold = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        for (UserCertificate userCertificate : userCertificates) {
            Instant expiry = userCertificate.getExpiryDate();
            if (expiry == null) {
                continue;
            }
            if (expiry.isBefore(now)) {
                expired++;
            } else if (expiry.isBefore(expiringSoonThreshold)) {
                expiringSoon++;
            } else {
                valid++;
            }
        }

        return CertificateStatsResponseDTO.builder()
                .total(total)
                .valid(valid)
                .expired(expired)
                .expiringSoon(expiringSoon)
                .build();
    }

    @Override
    public CertificateSummaryResponseDTO getCertificateSummary(CurrentUserContext context, Boolean isClientAdmin) {
        String userId = context.getUserId();

        List<UserCertificate> userCertificates = isClientAdmin != null && isClientAdmin ?
                userCertificateRepository.findByClientAdminId(userId) :
                userCertificateRepository.findByUserId(userId);

        Instant now = Instant.now();
        int validCount = 0;
        int expiredCount = 0;
        int expiringSoonCount = 0;
        int notCompleteCount = 0;

        for (UserCertificate userCertificate : userCertificates) {
            Instant expiry = userCertificate.getExpiryDate();
            if (expiry == null) {
                notCompleteCount++;
            } else if (expiry.isBefore(now)) {
                expiredCount++;
            } else if (expiry.isBefore(now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS))) {
                expiringSoonCount++;
            } else {
                validCount++;
            }
        }

        log.debug("Certificate summary - Valid: {}, Expired: {}, Expiring Soon: {}, Not Complete: {}", validCount, expiredCount, expiringSoonCount, notCompleteCount);

        return CertificateSummaryResponseDTO.builder()
                .validCount(validCount)
                .expiredCount(expiredCount)
                .expiringSoonCount(expiringSoonCount)
                .notCompleteCount(notCompleteCount)
                .build();
    }

    @Override
    public CertificateLinksDTO generateCertificate(String userId, String subPackageName, String subPackageId, String certificateId, String clientAdminId, Boolean isTrial) {
        String recipientName = userCurrentContextService.getCurrentUserContext().getFullName();
        return generateCertificate(userId, subPackageName, subPackageId, certificateId, clientAdminId, recipientName, isTrial);
    }

    @Override
    public CertificateLinksDTO generateCertificate(String userId, String subPackageName, String subPackageId,
                                                   String certificateId, String clientAdminId, String recipientFullName, Boolean isTrial) {
        String recipientName = recipientFullName != null ? recipientFullName : "";
        log.info("Recipient Name: {}", recipientName);
        String dateStr = LocalDate.now().toString();

        ProductInfo productInfo = fetchProductInfoByPackageId(subPackageId);
        subPackageName = productInfo.productName != null ? productInfo.productName : subPackageName;

        final var formatedCourseName = sanitizeName(subPackageName);

        String currentTime = String.valueOf(System.currentTimeMillis());
        String pdfFileName = String.format("Certificate_%s_%s_%s.pdf", userId, formatedCourseName, currentTime);
        String imageFileName = String.format("Certificate_%s_%s_%s.png", userId, formatedCourseName, currentTime);

        Path tmpPdf = null;
        Path tmpPng = null;

        try {
            // Fetch certificate template
            CertificateTemplate template = fetchCertificateTemplate(clientAdminId, isTrial);
            if (template == null) {
                log.error("No certificate template found for clientAdminId: {}", clientAdminId);
                return new CertificateLinksDTO(null, null, null);
            }

            // Extract all template fields
            String certificateTitle = template.getCertificateTitle();
            String certificateType = template.getCertificateType();
            String acknowledgement = template.getAcknowledgement();
            String completionStatus = template.getCompletionStatus();
            String completionTitle = template.getCompletionTitle();
            String logoImageUrl = template.getLogoImageUrl();
            String signatureImageUrl = template.getSignatureImageUrl();
            String signerName = template.getSignerName();
            String signerDesignation = template.getSignerDesignation();
            String signatureIdentity = template.getSignatureIdentity();
            String backgroundImageUrl = template.getBackgroundImageUrl();
            String signatureUrl = fileService.buildUrl(signatureImageUrl);
            String logoUrl = fileService.buildUrl(logoImageUrl);
            String backgroundUrl = fileService.buildUrl(backgroundImageUrl);
            // Generate PDF to temp file using template fields
            tmpPdf = Files.createTempFile("cert_", ".pdf");
            
            generateCertificatePdf(tmpPdf, recipientName, subPackageName, dateStr, certificateId,
                    certificateTitle, certificateType, acknowledgement, completionStatus,
                    completionTitle, logoUrl, signatureUrl, signerName,
                    signerDesignation, signatureIdentity, backgroundUrl);

            // Upload PDF
            String pdfUrl = uploadFile(tmpPdf, pdfFileName);

            // Generate PNG from the same PDF
            tmpPng = Files.createTempFile("cert_", ".png");
            generateCertificateImage(tmpPdf, tmpPng);

            // Upload PNG
            String imageUrl = uploadFile(tmpPng, imageFileName);

            return new CertificateLinksDTO(pdfUrl, imageUrl, subPackageName);

        } catch (Exception e) {
            log.error("Certificate generation failed for userId={} packageId={}", userId, subPackageName, e);
            return new CertificateLinksDTO(null, null, null);

        } finally {
            deleteIfExists(tmpPdf);
            deleteIfExists(tmpPng);
        }
    }

    /**
     * Fetch certificate template for the given client admin ID.
     * Logic:
     * 1. Fetch ClientAdmin from registration service
     * 2. If onboardBy is TRIAL, use trial template (findByIsTrialTemplateTrue)
     * 3. Otherwise, try to fetch client-specific template from ClientCertificateTemplate
     * 4. If not found, use default template
     *
     * @param clientAdminId The client admin ID
     * @return CertificateTemplate or null if not found
     */
    private CertificateTemplate fetchCertificateTemplate(String clientAdminId, Boolean isTrial) {
        try {
            if (clientAdminId == null || clientAdminId.isBlank()) {
                log.warn("Client admin ID is null or blank, using default template");
                return certificateTemplateRepository.findByIsDefaultTrue().orElse(null);
            }
            
            // If onboardBy is TRIAL, use trial template
            if ( isTrial != null && isTrial) {
                log.debug("Client admin {} is onboarded via TRIAL, using trial template", clientAdminId);
                return certificateTemplateRepository.findByIsTrialTemplateTrue()
                        .orElseGet(() -> {
                            log.warn("Trial template not found, falling back to default template");
                            return certificateTemplateRepository.findByIsDefaultTrue().orElse(null);
                        });
            }

            // For non-TRIAL clients, try to fetch client-specific template
            java.util.Optional<ClientCertificateTemplate> clientTemplateOpt =
                    clientCertificateTemplateRepository.findByClientAdminIdAndActive(clientAdminId, true);

            if (clientTemplateOpt.isPresent()) {
                String templateId = clientTemplateOpt.get().getTemplateId();
                log.debug("Found client-specific template for clientAdminId: {}, templateId: {}", clientAdminId, templateId);
                
                return certificateTemplateRepository.findById(templateId)
                        .orElse(null);
            }

            // If no client-specific template, use default template
            log.debug("No client-specific template found, using default template");
            return certificateTemplateRepository.findByIsDefaultTrue()
                    .orElse(null);

        } catch (Exception e) {
            log.error("Error fetching certificate template for clientAdminId: {}", clientAdminId, e);
            // Fallback to default template
            try {
                return certificateTemplateRepository.findByIsDefaultTrue()
                        .orElse(null);
            } catch (Exception ex) {
                log.error("Error fetching default certificate template", ex);
                return null;
            }
        }
    }

    /**
     * Fetch ClientAdmin's onboardBy value from registration service.
     * Makes a REST call to /registration/api/v1/client/admin/{clientAdminId}
     *
     * @param clientAdminId The client admin ID
     * @return onboardBy value as String (TRIAL, BUY_NOW, MSP, ASPIRE_ADMIN) or null if not found/error
     */
    private String fetchClientAdminOnboardBy(String clientAdminId) {
        try {
            String url = registrationUrl + "/client/admin/" + clientAdminId;
            log.debug("Fetching ClientAdmin onboardBy from registration service: {}", url);

            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration service response for clientAdminId: {}", clientAdminId);
                return null;
            }

            JsonNode dataNode = response.get("data");
            JsonNode onboardByNode = dataNode.get("onboardBy");
            
            if (onboardByNode != null && !onboardByNode.isNull()) {
                String onboardBy = onboardByNode.asText();
                log.debug("Fetched onboardBy value: {} for clientAdminId: {}", onboardBy, clientAdminId);
                return onboardBy;
            } else {
                log.debug("onboardBy field not found in response for clientAdminId: {}", clientAdminId);
                return null;
            }

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Client admin not found in registration service: {}", clientAdminId);
            return null;
        } catch (WebClientResponseException e) {
            log.error("Error calling registration service for clientAdminId: {}. Status: {}, Response: {}", 
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching ClientAdmin onboardBy for clientAdminId: {}", clientAdminId, e);
            return null;
        }
    }

    private String sanitizeName(String productName) {
        return productName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void generateCertificatePdf(Path pdfPath, String recipient, String course, String date, String templateId) throws IOException {
        try (OutputStream out = Files.newOutputStream(pdfPath)) {
            certificateGenerator.generateCertificatePdfToStream(recipient, course, date, templateId, out);
        }
    }

    /**
     * Generate certificate PDF using all template fields.
     *
     * @param pdfPath The path where the PDF will be written
     * @param recipient The recipient's name
     * @param course The course name
     * @param date The certificate date
     * @param certificateId The certificate ID
     * @param certificateTitle The certificate title
     * @param certificateType The certificate type
     * @param acknowledgement The acknowledgement text
     * @param completionStatus The completion status text
     * @param completionTitle The completion title
     * @param logoImageUrl The logo image URL
     * @param signatureImageUrl The signature image URL
     * @param signerName The signer's name
     * @param signerDesignation The signer's designation
     * @param signatureIdentity The signature identity
     * @param backgroundImageUrl The background image URL
     * @throws IOException if PDF generation fails
     */
    private void generateCertificatePdf(Path pdfPath, String recipient, String course, String date, 
                                      String certificateId, String certificateTitle, String certificateType,
                                      String acknowledgement, String completionStatus, String completionTitle,
                                      String logoImageUrl, String signatureImageUrl, String signerName,
                                      String signerDesignation, String signatureIdentity, String backgroundImageUrl) throws IOException {
        try (OutputStream out = Files.newOutputStream(pdfPath)) {
            certificateGenerator.generateCertificatePdfToStream(recipient, course, date, certificateId,
                    certificateTitle, certificateType, acknowledgement, completionStatus,
                    completionTitle, logoImageUrl, signatureImageUrl, signerName,
                    signerDesignation, signatureIdentity, backgroundImageUrl, out);
        }
    }

    private void generateCertificateImage(Path pdfPath, Path pngPath) throws IOException {
        try (InputStream pdfIn = Files.newInputStream(pdfPath);
             OutputStream pngOut = Files.newOutputStream(pngPath)) {
            certificateGenerator.generateCertificateImageFromPdf(pdfIn, pngOut);
        }
    }

    private String uploadFile(Path filePath, String fileName) throws IOException {
        try {
            String uploadPath = "asatv2/uploads/dev/CERTIFICATES/" + fileName;
            return fileService.fileUpload(filePath.toString(), uploadPath).getPath();
        } catch (Exception e) {
            log.error("File upload failed for file: {}", filePath, e);
            throw new IOException("File upload failed", e);
        }
    }

    private void deleteIfExists(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                log.error("Failed to delete temp file: {}, {}", path, ex.getMessage());
            }
        }
    }

    @Override
    public List<CertificateDetailsResponseDTO> getCertificateDetails(CurrentUserContext context, Boolean isClientAdmin,
                                                                     String clientAdminId, String mspId,
                                                                     String certificateName, String productId,
                                                                     CertificateStatus status, int page, int size) {
        if (isMspCertificateDetailsRequest(context, mspId)) {
            return findCertificateDetailsByClientAdminIds(
                    resolveMspClientAdminIds(context, clientAdminId, mspId),
                    certificateName, productId, status, page, size).getContent();
        }

        String userId = context.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String search = normalizeSearch(certificateName);
        boolean isAdmin = Boolean.TRUE.equals(isClientAdmin);

        log.debug("Fetching certificate details for userId={}, isClientAdmin={}, certificateName={}, productId={}, status={}, page={}, size={}",
                userId, isAdmin, search, productId, status, page, size);

        try {
            List<UserCertificate> userCertificates;
            if (isAdmin) {
                userCertificates = userCertificateRepositoryCustom
                        .findByClientAdminIdWithSearch(userId, search, productId, status, pageable)
                        .getContent();
            } else {
                userCertificates = userCertificateRepositoryCustom
                        .findByUserIdWithSearch(userId, search, productId, status, pageable)
                        .getContent();
            }

            return userCertificates.stream()
                    .map(this::mapToCertificateDetailsResponseDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch certificate details for userId={}, isClientAdmin={}, certificateName={}, productId={}, status={}. Error: {}",
                    userId, isAdmin, search, productId, status, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public long countCertificateDetails(CurrentUserContext context, Boolean isClientAdmin,
                                        String clientAdminId, String mspId, String certificateName,
                                        String productId, CertificateStatus status) {
        if (isMspCertificateDetailsRequest(context, mspId)) {
            return countCertificateDetailsByClientAdminIds(
                    resolveMspClientAdminIds(context, clientAdminId, mspId),
                    certificateName, productId, status);
        }

        String userId = context.getUserId();
        String search = normalizeSearch(certificateName);
        boolean isAdmin = Boolean.TRUE.equals(isClientAdmin);

        log.debug("Counting certificate details for userId={}, isClientAdmin={}, certificateName={}, productId={}, status={}",
                userId, isClientAdmin, search, productId, status);

        try {
            long count;
            if (isAdmin) {
                count = userCertificateRepositoryCustom
                        .countByClientAdminIdWithSearch(userId, search, productId, status);
            } else {
                count = userCertificateRepositoryCustom
                        .countByUserIdWithSearch(userId, search, productId, status);
            }

            log.debug("Total certificate details found: {} for userId={}, isClientAdmin={}, certificateName={}, productId={}, status={}",
                    count, userId, isClientAdmin, search, productId, status);
            return count;

        } catch (Exception e) {
            log.error("Failed to count certificate details for userId={}, isClientAdmin={}, certificateName={}, productId={}, status={}. Error: {}",
                    userId, isClientAdmin, search, productId, status, e.getMessage(), e);
            return 0;
        }
    }

    private String normalizeSearch(String certificateName) {
        if (certificateName == null) {
            return null;
        }
        String trimmed = certificateName.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isMspCertificateDetailsRequest(CurrentUserContext context, String mspId) {
        if (mspId != null && !mspId.isBlank()) {
            return true;
        }
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Page<CertificateDetailsResponseDTO> findCertificateDetailsByClientAdminIds(
            List<String> clientAdminIds, String certificateName, String productId,
            CertificateStatus status, int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        String search = normalizeSearch(certificateName);

        if (clientAdminIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        log.debug("Fetching certificate details for clientAdminIds={}, certificateName={}, productId={}, status={}, offset={}, pageSize={}",
                clientAdminIds, search, productId, status, offset, pageSize);

        try {
            Page<UserCertificate> certificatesPage = search != null
                    ? userCertificateRepositoryCustom.findByClientAdminIdInWithSearch(
                            clientAdminIds, search, productId, status, pageable)
                    : userCertificateRepositoryCustom.findByClientAdminIdIn(
                            clientAdminIds, productId, status, pageable);

            List<CertificateDetailsResponseDTO> items = certificatesPage.getContent().stream()
                    .map(this::mapToCertificateDetailsResponseDTO)
                    .toList();

            return new PageImpl<>(items, pageable, certificatesPage.getTotalElements());
        } catch (Exception e) {
            log.error("Failed to fetch certificate details for clientAdminIds={}, certificateName={}, productId={}, status={}. Error: {}",
                    clientAdminIds, search, productId, status, e.getMessage(), e);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    private long countCertificateDetailsByClientAdminIds(
            List<String> clientAdminIds, String certificateName, String productId, CertificateStatus status) {
        if (clientAdminIds.isEmpty()) {
            return 0;
        }

        String search = normalizeSearch(certificateName);

        try {
            return search != null
                    ? userCertificateRepositoryCustom.countByClientAdminIdInWithSearch(
                            clientAdminIds, search, productId, status)
                    : userCertificateRepositoryCustom.countByClientAdminIdIn(
                            clientAdminIds, productId, status);
        } catch (Exception e) {
            log.error("Failed to count certificate details for clientAdminIds={}, certificateName={}, productId={}, status={}. Error: {}",
                    clientAdminIds, search, productId, status, e.getMessage(), e);
            return 0;
        }
    }

    private List<String> resolveMspClientAdminIds(CurrentUserContext context, String clientAdminId, String mspId) {
        String normalizedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank()) ? clientAdminId.trim() : null;
        if (normalizedClientAdminId != null) {
            return List.of(normalizedClientAdminId);
        }

        List<String> contextClientAdminIds = context.getClientAdminIds();
        if (contextClientAdminIds != null && !contextClientAdminIds.isEmpty()) {
            return contextClientAdminIds;
        }

        String effectiveMspId = resolveMspId(context, mspId);
        if (effectiveMspId == null || effectiveMspId.isBlank()) {
            log.warn("Unable to resolve MSP ID for userId={}, userType={}", context.getUserId(), context.getUserType());
            return List.of();
        }
        return clientAdminServiceClient.getClientAdminIdsByMspId(effectiveMspId);
    }

    private String resolveMspId(CurrentUserContext context, String requestMspId) {
        UserType userType = UserType.fromString(context.getUserType());
        if (UserType.MSP.equals(userType)) {
            return context.getUserId();
        }
        return (requestMspId != null && !requestMspId.isBlank()) ? requestMspId.trim() : null;
    }

    private CertificateDetailsResponseDTO mapToCertificateDetailsResponseDTO(UserCertificate userCertificate) {
        CertificateStatus resolvedStatus = CertificateStatusUtil.resolveStatus(userCertificate.getExpiryDate());
        String status = resolvedStatus != null
                ? resolvedStatus.name()
                : userCertificate.getStatus();

        return CertificateDetailsResponseDTO.builder()
                .certificateId(userCertificate.getCertificateId())
                .userId(userCertificate.getUserId())
                .email(userCertificate.getUsername())
                .fullName(userCertificate.getFullName())
                .productName(userCertificate.getProductName())
                .subPackageId(userCertificate.getSubPackageId())
                .certificateUrl(userCertificate.getCertificateUrl())
                .certificateImageUrl(userCertificate.getImageCertificateLink())
                .status(status)
                .expiryDate(userCertificate.getExpiryDate())
                .createdAt(userCertificate.getCreatedAt())
                .build();
    }

    @Override
    public Page<ExamCertificateResponseDTO> getExamCertificates(
            String countryId, String mspId, String search, String clientAdminId, String subpackageId, int offset, int pageSize) {
        log.info("Fetching exam certificates - search: {}, clientAdminId: {}, subpackageId: {}, offset: {}, pageSize: {}", 
                search, clientAdminId, subpackageId, offset, pageSize);

        try {

            // Create pageable
            Pageable pageable = PageRequest.of(offset, pageSize);

            // Normalize search string
            String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
            String normalizedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank()) ? clientAdminId.trim() : null;
            String normalizedSubpackageId = (subpackageId != null && !subpackageId.isBlank()) ? subpackageId.trim() : null;
            String normalizedCountryId = (countryId != null && !countryId.isBlank()) ? countryId.trim() : null;
            String normalizedMspId = (mspId != null && !mspId.isBlank()) ? mspId.trim() : null;

            // Call custom repository method
            return userCertificateRepositoryCustom.findExamCertificatesWithJoin(
                    normalizedCountryId, normalizedMspId, normalizedSearch, normalizedClientAdminId, normalizedSubpackageId, pageable);

        } catch (Exception e) {
            log.error("Failed to fetch exam certificates - search: {}, clientAdminId: {}, subpackageId: {}, offset: {}, pageSize: {}. Error: {}", 
                    search, clientAdminId, subpackageId, offset, pageSize, e.getMessage(), e);
            // Return empty page on error
            return new PageImpl<>(
                    List.of(), 
                    PageRequest.of(0, pageSize), 
                    0L
            );
        }
    }

    @Override
    public CertificateSummaryStatsResponseDTO getCertificateSummaryStats(
            String clientAdminId, String mspId, String productId, Instant fromDate, Instant toDate) {
        log.info("Getting certificate summary statistics - clientAdminId: {}, mspId: {}, productId: {}, fromDate: {}, toDate: {}",
                clientAdminId, mspId, productId, fromDate, toDate);

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        if (isMspCertificateDetailsRequest(context, mspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(context, clientAdminId, mspId);
            if (clientAdminIds.isEmpty()) {
                return emptyCertificateSummaryStats();
            }
            try {
                return userCertificateRepositoryCustom.getCertificateSummaryStatsForClientAdmins(
                        clientAdminIds, productId, fromDate, toDate);
            } catch (Exception e) {
                log.error("Failed to get MSP certificate summary statistics - clientAdminIds: {}, productId: {}, fromDate: {}, toDate: {}. Error: {}",
                        clientAdminIds, productId, fromDate, toDate, e.getMessage(), e);
                return emptyCertificateSummaryStats();
            }
        }

        try {
            return userCertificateRepositoryCustom.getCertificateSummaryStats(productId, fromDate, toDate);
        } catch (Exception e) {
            log.error("Failed to get certificate summary statistics - productId: {}, fromDate: {}, toDate: {}. Error: {}",
                    productId, fromDate, toDate, e.getMessage(), e);
            return emptyCertificateSummaryStats();
        }
    }

    private CertificateSummaryStatsResponseDTO emptyCertificateSummaryStats() {
        return CertificateSummaryStatsResponseDTO.builder()
                .totalCertificatesIssued(0L)
                .activeCertificatesCount(0L)
                .averageCompletionRate(0.0)
                .expiringThisMonth(0L)
                .build();
    }

    @Override
    public ExamCertificateResponseDTO getExamCertificateByExamId(String examId) {
        log.info("Getting exam certificate by examId: {}", examId);

        try {
            if (examId == null || examId.isBlank()) {
                log.warn("ExamId is null or empty");
                return null;
            }

            return userCertificateRepositoryCustom.findExamCertificateByExamId(examId.trim());
        } catch (Exception e) {
            log.error("Failed to get exam certificate by examId: {}. Error: {}", examId, e.getMessage(), e);
            return null;
        }
    }

}
