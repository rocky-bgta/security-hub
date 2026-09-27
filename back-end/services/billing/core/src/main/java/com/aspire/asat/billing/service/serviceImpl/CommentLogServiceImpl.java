package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.CommentLogResponseDTO;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.Action;
import com.aspire.asat.billing.model.CommentLog;
import com.aspire.asat.billing.model.NextStep;
import com.aspire.asat.billing.repo.ActionRepository;
import com.aspire.asat.billing.repo.CommentLogRepository;
import com.aspire.asat.billing.repo.InvoiceRepository;
import com.aspire.asat.billing.repo.NextStepRepository;
import com.aspire.asat.billing.service.CommentLogService;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLogServiceImpl implements CommentLogService {

    private final CommentLogRepository commentLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final ActionRepository actionRepository;
    private final NextStepRepository nextStepRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public CommentLogResponseDTO createCommentLog(CommentLogRequestDTO requestDTO) {
        // Validate invoiceId is present
        if (requestDTO.getInvoiceId() == null || requestDTO.getInvoiceId().isBlank()) {
            throw new ResourceNotFoundException("Invoice ID is required to create a comment log");
        }
        
        log.info("Creating comment log for invoice: {}", requestDTO.getInvoiceId());

        // Validate invoice exists
        invoiceRepository.findById(requestDTO.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + requestDTO.getInvoiceId()));

        // Validate action exists
        Action action = actionRepository.findById(requestDTO.getActionTakenId())
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + requestDTO.getActionTakenId()));

        // Validate next step exists
        NextStep nextStep = nextStepRepository.findById(requestDTO.getNextStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + requestDTO.getNextStepId()));

        // Get user context - use from DTO if present (internal service call), otherwise get from CurrentUserContext (direct API call)
        String userName;
        String userRole;
        
        if (requestDTO.getUserName() != null && requestDTO.getUserRole() != null) {
            // Use values from DTO (internal service call)
            userName = requestDTO.getUserName();
            userRole = requestDTO.getUserRole();
            log.debug("Using user context from DTO: userName={}, userRole={}", userName, userRole);
        } else {
            // Get from CurrentUserContext (direct API call)
            try {
                CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                userName = userContext.getUsername();
                userRole = userContext.getUserType();
                log.debug("Using user context from CurrentUserContext: userName={}, userRole={}", userName, userRole);
            } catch (Exception e) {
                log.error("Failed to get user context from headers. Error: {}", e.getMessage());
                throw new ResourceNotFoundException("User context is required. Either provide userName and userRole in request, or ensure CurrentContext header is present.");
            }
        }

        // Create comment log
        // Set approved from request DTO, defaulting to false if not provided
        Boolean approved = requestDTO.getApproved() != null ? requestDTO.getApproved() : false;
        
        CommentLog commentLog = CommentLog.builder()
                .id(UUID.randomUUID().toString())
                .invoiceId(requestDTO.getInvoiceId())
                .date(Instant.now())
                .userName(userName)
                .userRole(userRole)
                .comment(requestDTO.getComment())
                .actionTakenId(requestDTO.getActionTakenId())
                .nextStepId(requestDTO.getNextStepId())
                .approved(approved)
                .createdAt(Instant.now())
                .build();

        commentLogRepository.save(commentLog);

        log.info("Comment log created successfully with ID: {}", commentLog.getId());

        return buildResponseDTO(commentLog, action, nextStep);
    }

    @Override
    public CommentLogResponseDTO getCommentLogById(String id) {
        log.info("Getting comment log by ID: {}", id);

        CommentLog commentLog = commentLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment log not found with ID: " + id));

        Action action = actionRepository.findById(commentLog.getActionTakenId())
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + commentLog.getActionTakenId()));

        NextStep nextStep = nextStepRepository.findById(commentLog.getNextStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + commentLog.getNextStepId()));

        return buildResponseDTO(commentLog, action, nextStep);
    }

    @Override
    public List<CommentLogResponseDTO> getCommentLogsByInvoiceId(String invoiceId) {
        log.info("Getting comment logs for invoice: {}", invoiceId);

        // Validate invoice exists
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with ID: " + invoiceId);
        }

        List<CommentLog> commentLogs = commentLogRepository.findByInvoiceIdOrderByDateDesc(invoiceId);

        return commentLogs.stream()
                .map(commentLog -> {
                    Action action = actionRepository.findById(commentLog.getActionTakenId())
                            .orElse(null);
                    NextStep nextStep = nextStepRepository.findById(commentLog.getNextStepId())
                            .orElse(null);

                    return buildResponseDTO(commentLog, action, nextStep);
                })
                .toList();
    }

    @Override
    public CommentLogResponseDTO updateCommentLog(String id, CommentLogRequestDTO requestDTO) {
        log.info("Updating comment log with ID: {}", id);

        CommentLog commentLog = commentLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment log not found with ID: " + id));

        // Validate invoice exists
        if (!invoiceRepository.existsById(requestDTO.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found with ID: " + requestDTO.getInvoiceId());
        }

        // Validate action exists
        Action action = actionRepository.findById(requestDTO.getActionTakenId())
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with ID: " + requestDTO.getActionTakenId()));

        // Validate next step exists
        NextStep nextStep = nextStepRepository.findById(requestDTO.getNextStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Next step not found with ID: " + requestDTO.getNextStepId()));

        // Update comment log
        commentLog.setInvoiceId(requestDTO.getInvoiceId());
        commentLog.setComment(requestDTO.getComment());
        commentLog.setActionTakenId(requestDTO.getActionTakenId());
        commentLog.setNextStepId(requestDTO.getNextStepId());
        
        // Update approved field if provided in request, otherwise keep existing value
        if (requestDTO.getApproved() != null) {
            commentLog.setApproved(requestDTO.getApproved());
        }

        commentLogRepository.save(commentLog);

        log.info("Comment log updated successfully with ID: {}", id);

        return buildResponseDTO(commentLog, action, nextStep);
    }

    @Override
    public void deleteCommentLog(String id) {
        log.info("Deleting comment log with ID: {}", id);

        CommentLog commentLog = commentLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment log not found with ID: " + id));

        commentLogRepository.delete(commentLog);

        log.info("Comment log deleted successfully with ID: {}", id);
    }

    private CommentLogResponseDTO buildResponseDTO(CommentLog commentLog, Action action, NextStep nextStep) {
        return CommentLogResponseDTO.builder()
                .id(commentLog.getId())
                .invoiceId(commentLog.getInvoiceId())
                .date(commentLog.getDate())
                .userName(commentLog.getUserName())
                .userRole(commentLog.getUserRole())
                .comment(commentLog.getComment())
                .actionTakenId(commentLog.getActionTakenId())
                .actionName(action != null ? action.getName() : null)
                .nextStepId(commentLog.getNextStepId())
                .nextStepName(nextStep != null ? nextStep.getName() : null)
                .approved(commentLog.getApproved() != null ? commentLog.getApproved() : false)
                .createdAt(commentLog.getCreatedAt())
                .build();
    }
}

