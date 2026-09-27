package com.aspire.asat.universal.service.impl;


import com.aspire.asat.universal.entity.SupportTicketType;
import com.aspire.asat.universal.exception.ResourceNotFoundException;
import com.aspire.asat.universal.repository.SupportTicketTypeRepository;
import com.aspire.asat.universal.service.SupportTicketTypeService;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeRequestDTO;
import com.aspire.asat.universal.supportTicket.SupportTicketTypeResponseDTO;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketTypeServiceImpl implements SupportTicketTypeService {

    private final SupportTicketTypeRepository supportTicketTypeRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public AllResponseDto<List<SupportTicketTypeResponseDTO>> getAllSupportTicketTypes(String search, Boolean active,
                                                                                       int offset, int pageSize,
                                                                                       String sortBy, String sortDirection) {
        log.info("Getting all support ticket types with search: {}, active: {}, offset: {}, pageSize: {}",
                search, active, offset, pageSize);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(offset, pageSize, sort);

        Page<SupportTicketType> page = supportTicketTypeRepository.findAllWithFilters(
                search != null ? search : "", active, pageable);

        List<SupportTicketTypeResponseDTO> items = page.getContent().stream()
                .map(this::convertToResponseDTO)
                .toList();

        return new AllResponseDto<>(offset, pageSize, page.getTotalElements(), items);
    }

    @Override
    public SupportTicketTypeResponseDTO getSupportTicketTypeById(String id) {
        log.info("Getting support ticket type by id: {}", id);

        SupportTicketType supportTicketType = supportTicketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket type not found with id: " + id));

        return convertToResponseDTO(supportTicketType);
    }

    @Override
    public SupportTicketTypeResponseDTO createSupportTicketType(SupportTicketTypeRequestDTO request) {
        log.info("Creating support ticket type with name: {}", request.getName());

        String currentUserId = userCurrentContextService.getCurrentUserContext().getUserId();

        SupportTicketType supportTicketType = new SupportTicketType();
        supportTicketType.setName(request.getName());
        supportTicketType.setDescription(request.getDescription());
        supportTicketType.setCreatedBy(currentUserId);
        supportTicketType.setCreatedAt(Instant.now());
        supportTicketType.setActive(true);

        SupportTicketType saved = supportTicketTypeRepository.save(supportTicketType);
        log.info("Created support ticket type with id: {}", saved.getId());

        return convertToResponseDTO(saved);
    }

    @Override
    public void deleteSupportTicketType(String id) {
        log.info("Soft deleting support ticket type with id: {}", id);

        SupportTicketType supportTicketType = supportTicketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket type not found with id: " + id));

        String currentUserId = userCurrentContextService.getCurrentUserContext().getUserId();

        supportTicketType.setActive(false);
        supportTicketType.setUpdatedBy(currentUserId);
        supportTicketType.setUpdatedAt(Instant.now());

        supportTicketTypeRepository.save(supportTicketType);
        log.info("Soft deleted support ticket type with id: {}", id);
    }

    private SupportTicketTypeResponseDTO convertToResponseDTO(SupportTicketType entity) {
        SupportTicketTypeResponseDTO dto = new SupportTicketTypeResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setActive(entity.getActive());
        return dto;
    }

    @Override
    public SupportTicketTypeResponseDTO updateSupportTicketType(String id, SupportTicketTypeRequestDTO request) {
        log.info("Updating support ticket type with id: {}", id);

        SupportTicketType supportTicketType = supportTicketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket type not found with id: " + id));

        String currentUserId = userCurrentContextService.getCurrentUserContext().getUserId();

        supportTicketType.setName(request.getName());
        supportTicketType.setDescription(request.getDescription());
        if(request.getActive() != null) {
            supportTicketType.setActive(request.getActive());
        }
        supportTicketType.setUpdatedBy(currentUserId);
        supportTicketType.setUpdatedAt(Instant.now());

        SupportTicketType updated = supportTicketTypeRepository.save(supportTicketType);
        log.info("Updated support ticket type with id: {}", updated.getId());

        return convertToResponseDTO(updated);
    }

    @Override
    public List<SupportTicketTypeResponseDTO> getAllActiveSupportTicketTypes() {
        log.info("Getting all active support ticket types without pagination");

        List<SupportTicketType> supportTicketTypes = supportTicketTypeRepository.findByActiveTrue();

        return supportTicketTypes.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

}
