package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.common.dto.invoice_logs.ActionRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.ActionResponseDTO;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.Action;
import com.aspire.asat.billing.repo.ActionRepository;
import com.aspire.asat.billing.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;

    @Override
    public ActionResponseDTO createAction(ActionRequestDTO requestDTO) {
        log.info("Creating action with name: {}", requestDTO.getName());

        // Check if action with same name already exists
        if (actionRepository.existsByName(requestDTO.getName())) {
            throw new BillingServiceException(
                    "Action with name '" + requestDTO.getName() + "' already exists",
                    HttpStatus.CONFLICT
            );
        }

        Action action = Action.builder()
                .id(UUID.randomUUID().toString())
                .name(requestDTO.getName())
                .build();

        actionRepository.save(action);

        log.info("Action created successfully with ID: {}", action.getId());

        return ActionResponseDTO.builder()
                .id(action.getId())
                .name(action.getName())
                .build();
    }

    @Override
    public ActionResponseDTO getActionById(String id) {
        log.info("Getting action by ID: {}", id);

        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + id));

        return ActionResponseDTO.builder()
                .id(action.getId())
                .name(action.getName())
                .build();
    }

    @Override
    public List<ActionResponseDTO> getAllActions() {
        log.info("Getting all actions");

        List<Action> actions = actionRepository.findAll();

        return actions.stream()
                .map(action -> ActionResponseDTO.builder()
                        .id(action.getId())
                        .name(action.getName())
                        .build())
                .toList();
    }

    @Override
    public ActionResponseDTO updateAction(String id, ActionRequestDTO requestDTO) {
        log.info("Updating action with ID: {}", id);

        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + id));

        // Check if another action with same name exists (excluding current one)
        Action existingAction = actionRepository.findByName(requestDTO.getName()).orElse(null);
        if (existingAction != null && !existingAction.getId().equals(id)) {
            throw new BillingServiceException(
                    "Action with name '" + requestDTO.getName() + "' already exists",
                    HttpStatus.CONFLICT
            );
        }

        action.setName(requestDTO.getName());
        actionRepository.save(action);

        log.info("Action updated successfully with ID: {}", id);

        return ActionResponseDTO.builder()
                .id(action.getId())
                .name(action.getName())
                .build();
    }

    @Override
    public void deleteAction(String id) {
        log.info("Deleting action with ID: {}", id);

        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + id));

        actionRepository.delete(action);

        log.info("Action deleted successfully with ID: {}", id);
    }
}

