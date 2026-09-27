package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.common.dto.invoice_logs.NextStepRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.NextStepResponseDTO;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.NextStep;
import com.aspire.asat.billing.repo.NextStepRepository;
import com.aspire.asat.billing.service.NextStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NextStepServiceImpl implements NextStepService {

    private final NextStepRepository nextStepRepository;

    @Override
    public NextStepResponseDTO createNextStep(NextStepRequestDTO requestDTO) {
        log.info("Creating next step with name: {}", requestDTO.getName());

        // Check if next step with same name already exists
        if (nextStepRepository.existsByName(requestDTO.getName())) {
            throw new BillingServiceException(
                    "Next step with name '" + requestDTO.getName() + "' already exists",
                    HttpStatus.CONFLICT
            );
        }

        NextStep nextStep = NextStep.builder()
                .id(UUID.randomUUID().toString())
                .name(requestDTO.getName())
                .build();

        nextStepRepository.save(nextStep);

        log.info("Next step created successfully with ID: {}", nextStep.getId());

        return NextStepResponseDTO.builder()
                .id(nextStep.getId())
                .name(nextStep.getName())
                .build();
    }

    @Override
    public NextStepResponseDTO getNextStepById(String id) {
        log.info("Getting next step by ID: {}", id);

        NextStep nextStep = nextStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + id));

        return NextStepResponseDTO.builder()
                .id(nextStep.getId())
                .name(nextStep.getName())
                .build();
    }

    @Override
    public List<NextStepResponseDTO> getAllNextSteps() {
        log.info("Getting all next steps");

        List<NextStep> nextSteps = nextStepRepository.findAll();

        return nextSteps.stream()
                .map(nextStep -> NextStepResponseDTO.builder()
                        .id(nextStep.getId())
                        .name(nextStep.getName())
                        .build())
                .toList();
    }

    @Override
    public NextStepResponseDTO updateNextStep(String id, NextStepRequestDTO requestDTO) {
        log.info("Updating next step with ID: {}", id);

        NextStep nextStep = nextStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + id));

        // Check if another next step with same name exists (excluding current one)
        NextStep existingNextStep = nextStepRepository.findByName(requestDTO.getName()).orElse(null);
        if (existingNextStep != null && !existingNextStep.getId().equals(id)) {
            throw new BillingServiceException(
                    "Next step with name '" + requestDTO.getName() + "' already exists",
                    HttpStatus.CONFLICT
            );
        }

        nextStep.setName(requestDTO.getName());
        nextStepRepository.save(nextStep);

        log.info("Next step updated successfully with ID: {}", id);

        return NextStepResponseDTO.builder()
                .id(nextStep.getId())
                .name(nextStep.getName())
                .build();
    }

    @Override
    public void deleteNextStep(String id) {
        log.info("Deleting next step with ID: {}", id);

        NextStep nextStep = nextStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + id));

        nextStepRepository.delete(nextStep);

        log.info("Next step deleted successfully with ID: {}", id);
    }
}

