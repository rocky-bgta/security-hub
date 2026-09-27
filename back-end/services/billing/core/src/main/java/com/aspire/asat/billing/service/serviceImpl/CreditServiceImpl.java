package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.exception.*;
import com.aspire.asat.billing.model.Credit;
import com.aspire.asat.billing.model.CreditTransaction;
import com.aspire.asat.billing.repo.CreditRepository;
import com.aspire.asat.billing.repo.CreditTransactionRepository;
import com.aspire.asat.billing.repo.customRepo.CreditTransactionRepositoryCustom;
import com.aspire.asat.billing.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceImpl implements CreditService {

    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private CreditTransactionRepository transactionRepository;
    @Autowired
    private CreditTransactionRepositoryCustom creditTransactionRepositoryCustom;

    @Override
    public CreditResponseDTO createCredit(CreditCreateRequestDTO requestDTO) {
        if (creditRepository.existsByClientId(requestDTO.getClientId())) {
            throw new CreditAlreadyExistsException(requestDTO.getClientId());
        }

        Credit credit = new Credit();
        credit.setClientId(requestDTO.getClientId());
        credit.setAvailableAmount(requestDTO.getCreditAmount());
        credit.setExpirationDate(requestDTO.getExpirationDate());
        credit.setRemarks(requestDTO.getReason());
        credit.setActive(true);
        credit.setCreatedAt(Instant.now());
        credit.setUpdatedAt(Instant.now());

        try {
            Credit saved = creditRepository.save(credit);
            return mapToCreditResponse(saved);
        } catch (Exception e) {
            throw new BillingServiceException("Failed to create credit entry", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public CreditResponseDTO updateCredit(String creditId, CreditUpdateRequestDTO requestDTO) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit not found for ID: " + creditId));

//        If the clientId is provided in the request, check if it already exists

//        if (requestDTO.getClientId() != null && !requestDTO.getClientId().equals(credit.getClientId())) {
//            if (creditRepository.existsByClientId(requestDTO.getClientId())) {
//                throw new CreditAlreadyExistsException(requestDTO.getClientId());
//            }
//            credit.setClientId(requestDTO.getClientId());
//        }

        credit.setAvailableAmount(requestDTO.getCreditAmount());
        credit.setExpirationDate(requestDTO.getExpirationDate());
        credit.setRemarks(requestDTO.getReason());
        credit.setUpdatedAt(Instant.now());

        Credit updated = creditRepository.save(credit);
        return mapToCreditResponse(updated);
    }

    @Override
    public CreditResponseDTO getCreditById(String creditId) {
        Credit credit = creditRepository.findById(creditId).orElseThrow(() -> new ResourceNotFoundException("Credit not found with ID: " + creditId));
        return mapToCreditResponse(credit);
    }

    @Override
    public List<CreditResponseDTO> getCreditsByClientId(String clientId) {
        Credit credit = creditRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Credit not found for clientId: " + clientId));
        return List.of(mapToCreditResponse(credit));
    }


    @Override
    public CreditResponseDTO deactivateCredit(String creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit not found with ID: " + creditId));

        boolean currentStatus = credit.isActive();
        credit.setActive(!currentStatus); // Toggle active status
        credit.setUpdatedAt(Instant.now());

        String action = currentStatus ? "deactivated" : "reactivated";
        log.info("Credit with ID {} has been {}", creditId, action);

        Credit updated = creditRepository.save(credit);
        return mapToCreditResponse(updated);
    }


    @Override
    public void deleteCredit(String creditId) {
        Credit credit = creditRepository.findById(creditId).orElseThrow(() -> new ResourceNotFoundException("Credit not found with ID: " + creditId));
        creditRepository.delete(credit);
    }

    @Override
    public CreditTransactionResponseDTO logCreditTransaction(CreditTransactionCreateDTO dto) {
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new InvalidTransactionAmountException("Amount must be greater than zero");
        }

        CreditTransaction transaction = new CreditTransaction();
        String id = UUID.randomUUID().toString();
        transaction.setId(id);
        transaction.setClientId(dto.getClientId());
        transaction.setType(dto.getType());
        transaction.setStatus(dto.getStatus() != null ? dto.getStatus() : TransactionStatus.UNPAID);
        transaction.setAmount(dto.getAmount());
        transaction.setReferenceType(dto.getReferenceType());
        transaction.setReferenceId(dto.getReferenceId());
        transaction.setRemarks(dto.getDescription());
        transaction.setCreatedAt(Instant.now());
        transaction.setReversed(false);

        CreditTransaction saved = transactionRepository.save(transaction);

        return CreditTransactionResponseDTO.builder()
                .id(saved.getId())
                .clientId(saved.getClientId())
                .type(saved.getType())
                .amount(saved.getAmount())
                .description(saved.getRemarks())
                .referenceId(saved.getReferenceId())
                .referenceType(saved.getReferenceType())
                .initiatedBy(dto.getInitiatedBy())
                .createdAt(saved.getCreatedAt())
                .build();
    }


    @Override
    public CreditTransactionResponseDTO getCreditTransactionById(String transactionId) {
        CreditTransaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new ResourceNotFoundException("Credit transaction not found with ID: " + transactionId));
        return mapToTransactionResponse(transaction);
    }

    @Override
    public List<CreditTransactionResponseDTO> getTransactionsByCreditId(String creditId) {
        List<CreditTransaction> transactions = transactionRepository.findByReferenceTypeAndReferenceId("CREDIT", creditId);
        return transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList());
    }

    @Override
    public List<CreditTransactionResponseDTO> getTransactionsByClientId(String clientId) {
        List<CreditTransaction> transactions = transactionRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList());
    }

    @Override
    public CreditTransferResponseDTO transferCredit(CreditTransferRequestDTO requestDTO) {
        if (requestDTO.getFromClientId().equals(requestDTO.getToClientId())) {
            throw new CreditTransferToSelfException("Cannot transfer credits to the same client.");
        }
        // Fetch source credit account
        Credit fromCredit = creditRepository.findByClientId(requestDTO.getFromClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Source credit account not found."));

        // Fetch target credit account
        Credit toCredit = creditRepository.findByClientId(requestDTO.getToClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Target credit account not found."));

        // Check if both accounts are active
        if (!fromCredit.isActive()) {
            throw new CreditAccountInactiveException("Source credit account is inactive.");
        }
        if (!toCredit.isActive()) {
            throw new CreditAccountInactiveException("Target credit account is inactive.");
        }

        // Validate sufficient balance
        if (fromCredit.getAvailableAmount() < requestDTO.getAmount()) {
            throw new InsufficientCreditBalanceException("Insufficient credit balance for transfer.");
        }

        // Deduct from sender
        fromCredit.setAvailableAmount(fromCredit.getAvailableAmount() - requestDTO.getAmount());
        fromCredit.setUpdatedAt(Instant.now());
        creditRepository.save(fromCredit);

        // Add to receiver
        toCredit.setAvailableAmount(toCredit.getAvailableAmount() + requestDTO.getAmount());
        toCredit.setUpdatedAt(Instant.now());
        creditRepository.save(toCredit);

        // Log sender transaction (OUT)
        CreditTransaction outTx = new CreditTransaction();
        outTx.setClientId(fromCredit.getClientId());
        outTx.setType("TRANSFER_OUT");
        outTx.setAmount(requestDTO.getAmount());
        outTx.setReferenceType("CREDIT_TRANSFER");
        outTx.setReferenceId(toCredit.getClientId()); // where it was sent
        outTx.setRemarks(requestDTO.getReason());
        outTx.setCreatedAt(Instant.now());
        transactionRepository.save(outTx);

        // Log receiver transaction (IN)
        CreditTransaction inTx = new CreditTransaction();
        inTx.setClientId(toCredit.getClientId());
        inTx.setType("TRANSFER_IN");
        inTx.setAmount(requestDTO.getAmount());
        inTx.setReferenceType("CREDIT_TRANSFER");
        inTx.setReferenceId(fromCredit.getClientId()); // from whom it was received
        inTx.setRemarks(requestDTO.getReason());
        inTx.setCreatedAt(Instant.now());
        transactionRepository.save(inTx);

        return CreditTransferResponseDTO.builder()
                .fromClientId(fromCredit.getClientId())
                .toClientId(toCredit.getClientId())
                .amountTransferred(requestDTO.getAmount())
                .message("Credits transferred successfully")
                .build();
    }

    @Override
    public CreditResponseDTO depositCredit(CreditOperationRequestDTO dto) {
        Credit credit = creditRepository.findByClientId(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit account not found"));

        credit.setAvailableAmount(credit.getAvailableAmount() + dto.getAmount());
        credit.setUpdatedAt(Instant.now());
        creditRepository.save(credit);

        CreditTransaction tx = new CreditTransaction();
        tx.setClientId(dto.getClientId());
        tx.setType("DEPOSIT");
        tx.setAmount(dto.getAmount());
        tx.setReferenceType("MANUAL_ADJUSTMENT");
        tx.setReferenceId(dto.getReferenceId());
        tx.setRemarks(dto.getReason());
        tx.setCreatedAt(Instant.now());
        transactionRepository.save(tx);

        return mapToCreditResponse(credit);
    }

    @Override
    public CreditResponseDTO withdrawCredit(CreditOperationRequestDTO dto) {
        Credit credit = creditRepository.findByClientId(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit account not found"));

        if (credit.getAvailableAmount() < dto.getAmount()) {
            throw new InsufficientCreditBalanceException("Insufficient balance for withdrawal.");
        }

        credit.setAvailableAmount(credit.getAvailableAmount() - dto.getAmount());
        credit.setUpdatedAt(Instant.now());
        creditRepository.save(credit);

        CreditTransaction tx = new CreditTransaction();
        tx.setClientId(dto.getClientId());
        tx.setType("USAGE");
        tx.setAmount(dto.getAmount());
        tx.setReferenceType("MANUAL_ADJUSTMENT");
        tx.setReferenceId(dto.getReferenceId());
        tx.setRemarks(dto.getReason());
        tx.setCreatedAt(Instant.now());
        transactionRepository.save(tx);

        return mapToCreditResponse(credit);
    }

    @Override
    public List<CreditTransactionResponseDTO> getCreditUsageSummaryByClientId(String clientId,
                                                                              String invoiceId,
                                                                              TransactionStatus status,
                                                                              String type,
                                                                              int offset,
                                                                              int limit) {

        // 1. Validate offset is a multiple of limit for correct pagination
        if (offset % limit != 0) {
            throw new BillingServiceException(
                "Offset must be a multiple of limit for pagination", 
                HttpStatus.BAD_REQUEST
            );
        }
        // 2. Prepare pageable
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. Fetch filtered paginated transactions
        List<CreditTransaction> filteredTransactions = creditTransactionRepositoryCustom
                .findTransactionsWithDynamicFilters(clientId, invoiceId, status, type, pageable);

        // 3. Count total matching transactions (ignoring pagination)
        long totalCount = creditTransactionRepositoryCustom
                .countFilteredTransactions(clientId, invoiceId, status, type);

        // 4. Map to DTO list
        List<CreditTransactionResponseDTO> responseDTOList = new ArrayList<>();
        for (CreditTransaction tx : filteredTransactions) {
            CreditTransactionResponseDTO dto = new CreditTransactionResponseDTO();
            dto.setId(tx.getId());
            dto.setClientId(tx.getClientId());
            dto.setType(tx.getType());
            dto.setAmount(tx.getAmount());
            dto.setReferenceType(tx.getReferenceType());
            dto.setReferenceId(tx.getReferenceId());
            dto.setInvoiceId(tx.getInvoiceId());
            dto.setRemarks(tx.getRemarks());
            dto.setStatus(tx.getStatus());
            dto.setReversed(tx.isReversed());
            dto.setCreatedAt(tx.getCreatedAt());
            dto.setDescription(tx.getRemarks() != null ? tx.getRemarks() : "Credit transaction");
            dto.setInitiatedBy("system");
            responseDTOList.add(dto);
        }

        // 5. Return paginated result
        return responseDTOList;
    }

    @Override
    public long countCreditUsageSummaryByClientId(String clientId, String invoiceId, TransactionStatus status, String type) {
        return creditTransactionRepositoryCustom.countFilteredTransactions(clientId, invoiceId, status, type);
    }

    @Override
    public CreditUsageSummaryDTO getCreditUsageTotalsByClientId(String clientId, String type) {

        // 2. Total paid = Only where status == PAID
        double totalPaidCredit = creditTransactionRepositoryCustom.calculatePaidCreditByType(clientId, type);

        // 3. Total due = All USAGE where status != PAID
        double totalDueCredit = creditTransactionRepositoryCustom.calculateDueCreditByType(clientId, type);

        return CreditUsageSummaryDTO.builder()
                .totalUsedCredit(totalPaidCredit+totalDueCredit)
                .totalPaidCredit(totalPaidCredit)
                .totalDueCredit(totalDueCredit)
                .build();
    }

    @Override
    public CreditTransactionResponseDTO updateCreditTransaction(CreditTransactionUpdateRequestDTO dto) {
        CreditTransaction transaction = transactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit transaction not found with ID: " + dto.getTransactionId()));

        if (dto.getStatus() != null) {
            transaction.setStatus(dto.getStatus());
        }
        if (dto.getReferenceId() != null) {
            transaction.setReferenceId(dto.getReferenceId());
        }
        if (dto.getRemarks() != null) {
            transaction.setRemarks(dto.getRemarks());
        }

        transactionRepository.save(transaction);

        return CreditTransactionResponseDTO.builder()
                .id(transaction.getId())
                .clientId(transaction.getClientId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getRemarks())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .status(transaction.getStatus())
                .invoiceId(transaction.getInvoiceId())
                .reversed(transaction.isReversed())
                .createdAt(transaction.getCreatedAt())
                .initiatedBy("system")
                .build();
    }






    private CreditResponseDTO mapToCreditResponse(Credit credit) {
        return CreditResponseDTO.builder().id(credit.getId()).clientId(credit.getClientId()).availableCredits(credit.getAvailableAmount()).totalCredits(credit.getAvailableAmount()).expirationDate(credit.getExpirationDate()).reason(credit.getRemarks()).createdAt(credit.getCreatedAt()).active(credit.isActive()).build();
    }

    private CreditTransactionResponseDTO mapToTransactionResponse(CreditTransaction tx) {
        return CreditTransactionResponseDTO.builder()
                .id(tx.getId())
                .clientId(tx.getClientId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .description(tx.getRemarks())
                .referenceId(tx.getReferenceId())
                .referenceType(tx.getReferenceType())
                .createdAt(tx.getCreatedAt())
                .build();
    }

}
