package com.aspire.asat.universal.service.impl;


import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.client.UniversalNotificationClient;
import com.aspire.asat.universal.data.externalresponses.ProductDetailsDto;
import com.aspire.asat.universal.data.externalresponses.SubPackageDetailResponseDto;
import com.aspire.asat.universal.data.externalresponses.UserDetailsDto;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.exception.ResourceNotFoundException;
import com.aspire.asat.universal.exception.UniversalServiceException;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.service.ExternalApiService;
import com.aspire.asat.universal.service.SupportTicketService;
import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.supportTicket.request.SupportTicketCreateRequestDto;
import com.aspire.asat.universal.supportTicket.request.SupportTicketUpdateRequestDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketGetResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketResponseDto;
import com.aspire.asat.universal.supportTicket.response.SupportTypeDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.utils.TicketIdGenerator;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ExternalApiService externalApiService;
    private final SupportTicketTypeRepository supportTicketTypeRepository;
    private final UniversalNotificationClient universalNotificationClient;

    // Static MSP Admin ID - to be configured via properties
    private static final String STATIC_MSP_ADMIN_ID = "msp-admin-default";

    @Override
    @Transactional
    public SupportTicketResponseDto createSupportTicket(SupportTicketCreateRequestDto requestDto) {
        log.info("Creating support ticket with title: {}", requestDto.getTitle());

        try {
            // Get current user context
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            String userType = currentUserContext.getUserType();
            String userId = currentUserContext.getUserId();
            String username = currentUserContext.getUsername();
            String clientAdminId = currentUserContext.getClientAdminId();


            log.info("Creating ticket - UserType: {}, UserId: {}, Username: {}, ClientAdminId: {}", userType, userId, username, clientAdminId);

            // Generate unique ticketId
            String ticketId = generateUniqueTicketId();
            log.info("Generated ticketId: {}", ticketId);

            // Build ticket based on user hierarchy
            SupportTicket.SupportTicketBuilder ticketBuilder = SupportTicket.builder()
                    .id(UUID.randomUUID().toString())
                    .ticketId(ticketId)
                    .title(requestDto.getTitle())
                    .parentTicketId(requestDto.getParentTicketId())
                    .status(TicketStatus.OPEN)
                    .priority(requestDto.getPriority())
                    .supportType(requestDto.getSupportType())
                    .courseId(requestDto.getCourseId())
                    .description(requestDto.getDescription())
                    .attachments(requestDto.getAttachments() != null ? new ArrayList<>(requestDto.getAttachments()) : new ArrayList<>())
                    .createdDate(Instant.now())
                    .updatedDate(Instant.now())
                    .assignToSuperAdmin(false);

            // Handle hierarchy based on user type
            if (UserTypeUtils.isEndUser(userType)) {
                // Case 1: clientUser/endUser → creates ticket to clientAdmin

                // Fetch product details from CMS service if courseId (subpackageId) is provided
                if (StringUtils.hasText(requestDto.getCourseId())) {
                    try {
                        Optional<SubPackageDetailResponseDto> subPackageDetails =
                                externalApiService.getSubPackageById(requestDto.getCourseId());

                        if (subPackageDetails.isPresent() && subPackageDetails.get().getProductDetails() != null) {
                            ProductDetailsDto productDetails = subPackageDetails.get().getProductDetails();
                            ticketBuilder
                                    .productId(productDetails.getId())
                                    .productName(productDetails.getProductName());
                            log.info("Fetched product details - productId: {}, productName: {} for subpackageId: {}",
                                    productDetails.getId(), productDetails.getProductName(), requestDto.getCourseId());
                        } else {
                            log.warn("Product details not found for subpackageId: {}, using provided productId if available",
                                    requestDto.getCourseId());
                            // Use provided productId if available
                            if (StringUtils.hasText(requestDto.getProductId())) {
                                ticketBuilder.productId(requestDto.getProductId());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error fetching product details for subpackageId: {}", requestDto.getCourseId(), e);
                        // Continue with provided productId if available
                        if (StringUtils.hasText(requestDto.getProductId())) {
                            ticketBuilder.productId(requestDto.getProductId());
                        }
                    }
                } else {
                    // Use provided productId if available
                    if (StringUtils.hasText(requestDto.getProductId())) {
                        ticketBuilder.productId(requestDto.getProductId());
                    }
                }

                ticketBuilder
                        .userId(userId)
                        .username(username)
                        .userType(userType)
                        .clientId(clientAdminId) // clientAdminId from context
                        .assignedTo(clientAdminId) // assigned to clientAdmin
                        .mspId(null)
                        .assignCategory(AssignCategory.ASSIGNTOCLIENT)
                        .createdBy(userId)
                        .updatedBy(userId);
                log.info("Ticket created by endUser {} assigned to clientAdmin {} with category ASSIGNTOCLIENT", userId, clientAdminId);

            } else if (UserTypeUtils.isClientAdmin(userType)) {
                // Case 2: clientAdmin → creates ticket to mspAdmin
                // Fetch MSP ID from registration service
                String mspId = externalApiService.getMspIdByClientAdminId(userId)
                        .orElse(STATIC_MSP_ADMIN_ID);

                if (mspId.equals(STATIC_MSP_ADMIN_ID)) {
                    log.warn("MSP ID not found for clientAdmin {}, using static MSP admin ID: {}", userId, STATIC_MSP_ADMIN_ID);
                } else {
                    log.info("Retrieved MSP ID: {} for clientAdmin {}", mspId, userId);
                }

                // Use provided productId if available
                if (StringUtils.hasText(requestDto.getProductId())) {
                    ticketBuilder.productId(requestDto.getProductId());
                }

                ticketBuilder
                        .userId(null) // clientAdmin is the user
                        .username(username)
                        .userType(userType)
                        .clientId(userId)
                        .assignedTo(mspId) // assigned to MSP admin (from registration service or static fallback)
                        .mspId(mspId)
                        .assignCategory(AssignCategory.ASSIGNTOMSP)
                        .createdBy(userId)
                        .updatedBy(userId);
                log.info("Ticket created by clientAdmin {} assigned to mspAdmin {} with category ASSIGNTOMSP", userId, mspId);

            } else if (UserTypeUtils.isMspAdmin(userType)) {
                // Case 3: mspAdmin → creates ticket to aspireAdmin/superAdmin

                // Use provided productId if available
                if (StringUtils.hasText(requestDto.getProductId())) {
                    ticketBuilder.productId(requestDto.getProductId());
                }

                ticketBuilder
                        .userId(null)
                        .username(username)
                        .userType(userType)
                        .clientId(null)
                        .assignedTo(null) // will be handled by assignToSuperAdmin flag
                        .mspId(userId) // mspAdmin ID
                        .assignToSuperAdmin(true)
                        .assignCategory(AssignCategory.ASSIGNTOSUPER)
                        .createdBy(userId)
                        .updatedBy(userId);
                log.info("Ticket created by mspAdmin {} assigned to superAdmin with category ASSIGNTOSUPER", userId);

            } else {
                throw new UniversalServiceException("Unsupported user type for ticket creation: " + userType);
            }

            SupportTicket ticket = ticketBuilder.build();
            SupportTicket savedTicket = supportTicketRepository.save(ticket);
            log.info("Support ticket created successfully with ID: {}", savedTicket.getId());

            // Send email notification to the assigned admin
            sendTicketCreatedNotification(savedTicket, currentUserContext);

            return mapToResponseDto(savedTicket);
        } catch (Exception e) {
            log.error("Error creating support ticket: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to create support ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public SupportTicketGetResponseDto getSupportTicketById(String id) {
        log.info("Fetching support ticket with ID: {}", id);

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));
        SupportTicketGetResponseDto responseDto = mapToGetResponseDto(ticket);

        // Fetch and populate supportType details
        if (StringUtils.hasText(ticket.getSupportType())) {
            try {
                Optional<SupportTicketType> supportTicketType = supportTicketTypeRepository.findById(ticket.getSupportType());
                if (supportTicketType.isPresent()) {
                    SupportTypeDto supportTypeDto = SupportTypeDto.builder()
                            .id(supportTicketType.get().getId())
                            .name(supportTicketType.get().getName())
                            .build();
                    responseDto.setSupportType(supportTypeDto);
                } else {
                    log.warn("Support ticket type not found with ID: {}", ticket.getSupportType());
                    // Set null or empty DTO if type not found
                    responseDto.setSupportType(null);
                }
            } catch (Exception e) {
                log.error("Error fetching support ticket type for ID: {}", ticket.getSupportType(), e);
                responseDto.setSupportType(null);
            }
        } else {
            responseDto.setSupportType(null);
        }

        return responseDto;
    }

    private SupportTicketGetResponseDto mapToGetResponseDto(SupportTicket ticket) {
        return SupportTicketGetResponseDto.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .title(ticket.getTitle())
                .clientId(ticket.getClientId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .userType(ticket.getUserType())
                .parentTicketId(ticket.getParentTicketId())
                .assignedTo(ticket.getAssignedTo())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .supportType(null) // Will be populated in getSupportTicketById method
                .productId(ticket.getProductId())
                .productName(ticket.getProductName())
                .packageId(ticket.getPackageId())
                .courseId(ticket.getCourseId())
                .mspId(ticket.getMspId())
                .description(ticket.getDescription())
                .attachments(ticket.getAttachments() != null ? new ArrayList<>(ticket.getAttachments()) : new ArrayList<>())
                .createdDate(ticket.getCreatedDate())
                .updatedDate(ticket.getUpdatedDate())
                .createdBy(ticket.getCreatedBy())
                .updatedBy(ticket.getUpdatedBy())
                .assignToSuperAdmin(ticket.getAssignToSuperAdmin())
                .assignCategory(ticket.getAssignCategory())
                .build();
    }

    @Override
    @Transactional
    public SupportTicketResponseDto updateSupportTicket(String id, SupportTicketUpdateRequestDto requestDto) {
        log.info("Updating support ticket with ID: {}", id);

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));

        try {
            // Update fields if provided
            if (StringUtils.hasText(requestDto.getTitle())) {
                ticket.setTitle(requestDto.getTitle());
            }
            if (StringUtils.hasText(requestDto.getUserId())) {
                ticket.setUserId(requestDto.getUserId());
            }
            if (StringUtils.hasText(requestDto.getParentTicketId())) {
                ticket.setParentTicketId(requestDto.getParentTicketId());
            }
            if (StringUtils.hasText(requestDto.getAssignedTo())) {
                ticket.setAssignedTo(requestDto.getAssignedTo());
            }
            if (requestDto.getStatus() != null) {
                ticket.setStatus(requestDto.getStatus());
            }
            if (requestDto.getPriority() != null) {
                ticket.setPriority(requestDto.getPriority());
            }
            if (requestDto.getSupportType() != null) {
                ticket.setSupportType(requestDto.getSupportType());
            }
            if (StringUtils.hasText(requestDto.getProductId())) {
                ticket.setProductId(requestDto.getProductId());
            }
            if (StringUtils.hasText(requestDto.getCourseId())) {
                ticket.setCourseId(requestDto.getCourseId());
            }
            if (StringUtils.hasText(requestDto.getMspId())) {
                ticket.setMspId(requestDto.getMspId());
            }
            // Note: productName and packageId are typically set automatically during creation
            // but can be updated manually if needed
            if (StringUtils.hasText(requestDto.getDescription())) {
                ticket.setDescription(requestDto.getDescription());
            }
            if (requestDto.getAttachments() != null) {
                ticket.setAttachments(new ArrayList<>(requestDto.getAttachments()));
            }
            if (StringUtils.hasText(requestDto.getUpdatedBy())) {
                ticket.setUpdatedBy(requestDto.getUpdatedBy());
            }

            ticket.setUpdatedDate(Instant.now());

            SupportTicket updatedTicket = supportTicketRepository.save(ticket);
            log.info("Support ticket updated successfully with ID: {}", updatedTicket.getId());

            return mapToResponseDto(updatedTicket);
        } catch (Exception e) {
            log.error("Error updating support ticket: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to update support ticket: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteSupportTicket(String id) {
        log.info("Deleting support ticket with ID: {}", id);

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));

        try {
            supportTicketRepository.delete(ticket);
            log.info("Support ticket deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting support ticket: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to delete support ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public AllResponseDto<List<SupportTicketResponseDto>> getAllSupportTickets(
            String clientId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            int offset,
            int pageSize) {

        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
        if (UserTypeUtils.isMspAdmin(currentUserContext.getUserType())) {
            mspId = currentUserContext.getUserId();
        }

        log.info("Fetching support tickets with filters - clientId: {}, assignedTo: {}, status: {}, priority: {}, supportType: {}, mspId: {}, assignToSuperAdmin: {}, createdBy: {}, assignCategory: {}, search: {}, ticketId: {}, offset: {}, pageSize: {}",
                clientId, assignedTo, status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, offset, pageSize);

        try {
            Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

            // Use custom repository method for complex filtering
            Page<SupportTicket> ticketPage = supportTicketRepository.findSupportTicketsWithFilters(
                    clientId,
                    null, // userId - not used in this method
                    null, // parentTicketId - not used in this method
                    assignedTo,
                    status,
                    priority,
                    supportType,
                    mspId,
                    assignToSuperAdmin,
                    createdBy,
                    assignCategory,
                    search,
                    pageable
            );

            List<SupportTicketResponseDto> ticketDtos = ticketPage.getContent().stream()
                    .map(this::mapToListResponseDto)
                    .collect(Collectors.toList());

            return new AllResponseDto<>(
                    offset,
                    pageSize,
                    ticketPage.getTotalElements(),
                    ticketDtos
            );

        } catch (Exception e) {
            log.error("Error fetching support tickets: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to fetch support tickets: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    public SupportTicketResponseDto updateTicketStatus(String id, TicketStatus status, String updatedBy) {
        log.info("Updating status of support ticket with ID: {} to {}", id, status);

        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));

        try {
            // Get current user context for updatedBy if not provided
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            if (!StringUtils.hasText(updatedBy)) {
                updatedBy = currentUserContext.getUserId();
            }

            ticket.setStatus(status);
            ticket.setUpdatedBy(updatedBy);
            ticket.setUpdatedDate(Instant.now());

            SupportTicket updatedTicket = supportTicketRepository.save(ticket);
            log.info("Ticket status updated successfully for ticket with ID: {}", updatedTicket.getId());

            // Send email notification for status update
            sendTicketStatusUpdateNotification(updatedTicket, status);

            return mapToResponseDto(updatedTicket);
        } catch (Exception e) {
            log.error("Error updating ticket status: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to update ticket status: " + e.getMessage(), e);
        }
    }

    /**
     * Send email and in-app notification when a support ticket is created.
     * The notification is sent to the assigned admin based on user hierarchy.
     *
     * @param ticket             The created support ticket
     * @param currentUserContext The current user context containing user details
     */
    private void sendTicketCreatedNotification(SupportTicket ticket, CurrentUserContext currentUserContext) {
        try {
            String recipientEmail = null;
            String recipientUserId = null;
            String recipientName = null;
            String clientAdminId = null;

            // Ticket created by end user → notify client admin
            recipientEmail = ObjectUtils.isEmpty(currentUserContext.getEmail()) ? currentUserContext.getClientAdminEmail() : currentUserContext.getEmail();
            recipientUserId = currentUserContext.getClientAdminId(); // Client admin ID for in-app notification
            recipientName = currentUserContext.getClientAdminFullName();
            clientAdminId = currentUserContext.getClientAdminId();

            if ((recipientEmail != null && !recipientEmail.isBlank()) || (recipientUserId != null && !recipientUserId.isBlank())) {
                universalNotificationClient.sendTicketCreatedNotification(
                        recipientEmail,
                        recipientUserId,
                        recipientName,
                        ticket.getTicketId(),
                        clientAdminId
                );
            } else {
                log.warn("Could not send ticket created notification: recipient email and userId not available for ticket {}",
                        ticket.getTicketId());
            }
        } catch (Exception e) {
            // Log error but don't fail the ticket creation
            log.error("Failed to send ticket created notification for ticket {}: {}",
                    ticket.getTicketId(), e.getMessage(), e);
        }
    }

    /**
     * Send email and in-app notification when a support ticket status is updated.
     * The notification is sent to the ticket creator to inform them of the status change.
     *
     * @param ticket             The updated support ticket
     * @param newStatus          The new ticket status
     */
    private void sendTicketStatusUpdateNotification(SupportTicket ticket, TicketStatus newStatus) {
        try {
            String recipientEmail = null;
            String recipientName = null;
            String clientAdminId = ticket.getClientId();

            log.info("Sending status update notification to end user for ticket: {}", ticket.getTicketId());

            // Fetch end user details from registration service
            Optional<UserDetailsDto> userDetails =
                    externalApiService.getEndUserDetails(ticket.getUserId());

            if (userDetails.isPresent()) {
                recipientEmail = userDetails.get().getEmail();
                recipientName = userDetails.get().getDisplayName();
            } else {
                log.warn("Could not fetch end user details for userId: {}, using fallback name", ticket.getUserId());
                recipientName = ticket.getUsername();
                recipientEmail = ticket.getUsername();
            }

            if ((recipientEmail != null && !recipientEmail.isBlank())) {
                universalNotificationClient.sendTicketStatusUpdateNotification(
                        recipientEmail,
                        ticket.getUserId(),
                        recipientName,
                        ticket.getTicketId(),
                        newStatus,
                        clientAdminId
                );
            } else {
                log.warn("Could not send ticket status update notification: recipient email and userId not available for ticket {}",
                        ticket.getTicketId());
            }
        } catch (Exception e) {
            // Log error but don't fail the status update
            log.error("Failed to send ticket status update notification for ticket {}: {}",
                    ticket.getTicketId(), e.getMessage(), e);
        }
    }

    /**
     * Generates a unique ticket ID in the format TKT-yyyymmdd-XXX
     * The sequence number is determined by counting existing tickets for the current date
     */
    private String generateUniqueTicketId() {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfNextDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Count existing tickets for today using custom repository method
        long existingTicketsCount = supportTicketRepository.countByCreatedDateBetween(startOfDay, startOfNextDay);

        // Generate next sequence number (1-based)
        int nextSequence = (int) existingTicketsCount + 1;

        // Generate ticket ID
        String ticketId = TicketIdGenerator.generateTicketId(today, nextSequence);

        // Ensure uniqueness (handle edge case where ticket was created between count and save)
        int maxRetries = 10;
        int retryCount = 0;
        while (supportTicketRepository.findByTicketId(ticketId).isPresent() && retryCount < maxRetries) {
            nextSequence++;
            ticketId = TicketIdGenerator.generateTicketId(today, nextSequence);
            retryCount++;
        }

        if (retryCount >= maxRetries) {
            throw new UniversalServiceException("Failed to generate unique ticket ID after " + maxRetries + " attempts");
        }

        return ticketId;
    }

    private SupportTicketResponseDto mapToResponseDto(SupportTicket ticket) {
        return SupportTicketResponseDto.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .title(ticket.getTitle())
                .clientId(ticket.getClientId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .userType(ticket.getUserType())
                .parentTicketId(ticket.getParentTicketId())
                .assignedTo(ticket.getAssignedTo())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .supportType(ticket.getSupportType())
                .productId(ticket.getProductId())
                .productName(ticket.getProductName())
                .packageId(ticket.getPackageId())
                .courseId(ticket.getCourseId())
                .mspId(ticket.getMspId())
                .description(ticket.getDescription())
                .attachments(ticket.getAttachments() != null ? new ArrayList<>(ticket.getAttachments()) : new ArrayList<>())
                .createdDate(ticket.getCreatedDate())
                .updatedDate(ticket.getUpdatedDate())
                .createdBy(ticket.getCreatedBy())
                .updatedBy(ticket.getUpdatedBy())
                .assignToSuperAdmin(ticket.getAssignToSuperAdmin())
                .assignCategory(ticket.getAssignCategory())
                .build();
    }

    private SupportTicketResponseDto mapToListResponseDto(SupportTicket ticket) {
        SupportTicketResponseDto response = SupportTicketResponseDto.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .title(ticket.getTitle())
                .clientId(ticket.getClientId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .userType(ticket.getUserType())
                .parentTicketId(ticket.getParentTicketId())
                .assignedTo(ticket.getAssignedTo())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .productId(ticket.getProductId())
                .productName(ticket.getProductName())
                .packageId(ticket.getPackageId())
                .courseId(ticket.getCourseId())
                .mspId(ticket.getMspId())
                .description(ticket.getDescription())
                .attachments(ticket.getAttachments() != null ? new ArrayList<>(ticket.getAttachments()) : new ArrayList<>())
                .createdDate(ticket.getCreatedDate())
                .updatedDate(ticket.getUpdatedDate())
                .createdBy(ticket.getCreatedBy())
                .updatedBy(ticket.getUpdatedBy())
                .assignToSuperAdmin(ticket.getAssignToSuperAdmin())
                .assignCategory(ticket.getAssignCategory())
                .build();

        // Fetch and populate supportType name
        if (StringUtils.hasText(ticket.getSupportType())) {
            try {
                Optional<SupportTicketType> supportTicketType = supportTicketTypeRepository.findById(ticket.getSupportType());
                if (supportTicketType.isPresent()) {
                    response.setSupportType(supportTicketType.get().getName());
                } else {
                    log.warn("Support ticket type not found with ID: {}", ticket.getSupportType());
                    response.setSupportType(null);
                }
            } catch (Exception e) {
                log.error("Error fetching support ticket type for ID: {}", ticket.getSupportType(), e);
                response.setSupportType(null);
            }
        } else {
            response.setSupportType(null);
        }

        return response;
    }
}

