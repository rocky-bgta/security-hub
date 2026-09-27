package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.MspTypeRequestDto;
import com.aspire.asat.registration.data.dropdown.MspTypeRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.MspType;
import com.aspire.asat.registration.repository.dropdown.MspTypeRepository;
import com.aspire.asat.registration.service.dropdown.MspTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class MspTypeServiceImpl implements MspTypeService {

    private final MspTypeRepository mspTypeRepository;

    @Override
    public MspTypeRespDto createMspType(MspTypeRequestDto request) {
        log.info("Creating MspType with name: {}", request.getName());

        if (mspTypeRepository.existsByName(request.getName())) {
            throw new DuplicateDataFoundException("MspType with name " + request.getName() + " already exists");
        }

        MspType mspType = MspType.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .isActive(request.getIsActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        MspType savedMspType = mspTypeRepository.save(mspType);
        log.info("Successfully created MspType with id: {}", savedMspType.getId());

        return mapToDto(savedMspType);
    }

    @Override
    public MspTypeRespDto getMspTypeById(String id) {
        log.info("Fetching MspType by id: {}", id);
        return mspTypeRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("MspType not found with id: " + id));
    }

    @Override
    public MspTypeRespDto updateMspType(String id, MspTypeRequestDto request) {
        log.info("Updating MspType with id: {}", id);

        MspType existingMspType = mspTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MspType not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!existingMspType.getName().equals(request.getName()) &&
                mspTypeRepository.existsByName(request.getName())) {
            throw new DuplicateDataFoundException("MspType with name " + request.getName() + " already exists");
        }

        existingMspType.setName(request.getName());
        existingMspType.setIsActive(request.getIsActive());
        existingMspType.setUpdatedAt(Instant.now());

        MspType updatedMspType = mspTypeRepository.save(existingMspType);
        log.info("Successfully updated MspType with id: {}", updatedMspType.getId());

        return mapToDto(updatedMspType);
    }

    @Override
    public List<MspTypeRespDto> getActiveMspTypes() {
        log.info("Fetching active MspTypes");
        return mspTypeRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMspType(String id) {
        log.info("Deleting MspType with id: {}", id);

        if (!mspTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("MspType not found with id: " + id);
        }

        mspTypeRepository.deleteById(id);
        log.info("Successfully deleted MspType with id: {}", id);
    }

    private MspTypeRespDto mapToDto(MspType mspType) {
        return MspTypeRespDto.builder()
                .id(mspType.getId())
                .name(mspType.getName())
                .isActive(mspType.getIsActive())
                .createdAt(mspType.getCreatedAt())
                .updatedAt(mspType.getUpdatedAt())
                .build();
    }
}
