package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.CommissionRateRequestDTO;
import com.aspire.asat.billing.dto.CommissionRateResponseDTO;
import com.aspire.asat.billing.dto.CreditTransactionCreateDTO;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceAlreadyExistsException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.CommissionRate;
import com.aspire.asat.billing.model.Credit;
import com.aspire.asat.billing.repo.CommissionRateRepository;
import com.aspire.asat.billing.repo.CreditRepository;
import com.aspire.asat.billing.service.CommissionRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommissionRateServiceImpl implements CommissionRateService {

    @Autowired
    private CommissionRateRepository commissionRateRepository;
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private CreditServiceImpl creditService;

    /**
     * Create a new commission rate for a client.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param requestDTO
     * @return
     */
    @Override
    public CommissionRateResponseDTO createCommissionRate(CommissionRateRequestDTO requestDTO) {
        Optional<CommissionRate> existing = commissionRateRepository.findByClientId(requestDTO.getClientId());
        if (existing.isPresent()) {
            throw new ResourceAlreadyExistsException("Commission rate already exists for client: " + requestDTO.getClientId());
        }

        CommissionRate rate = mapToModel(requestDTO);
        rate.setCreatedAt(Instant.now());
        rate.setUpdatedAt(Instant.now());
        return mapToResponse(commissionRateRepository.save(rate));
    }

    /**
     * Update an existing commission rate for a client.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param clientId
     * @param requestDTO
     * @return
     */
    @Override
    public CommissionRateResponseDTO updateCommissionRate(String clientId, CommissionRateRequestDTO requestDTO) {
        CommissionRate rate = commissionRateRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission rate not found for client: " + clientId));

        rate.setMspId(requestDTO.getMspId());
        rate.setCommissionPercentage(requestDTO.getCommissionPercentage());
        rate.setEligibilityCriteria(requestDTO.getEligibilityCriteria());
        rate.setMinInvoiceAmount(requestDTO.getMinInvoiceAmount());
        rate.setUpdatedAt(Instant.now());

        return mapToResponse(commissionRateRepository.save(rate));
    }

    /**
     * Get commission rate by client ID.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param clientId
     * @return
     */
    @Override
    public CommissionRateResponseDTO getCommissionRateByClientId(String clientId) {
        CommissionRate rate = commissionRateRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission rate not found for client: " + clientId));
        return mapToResponse(rate);
    }

    /**
     * List all commission rates with pagination.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<CommissionRateResponseDTO> listCommissionRates(int offset, int limit) {
        // Validate limit is positive
        if (limit <= 0) {
            throw new BillingServiceException(
                "Limit must be greater than 0", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate offset is non-negative
        if (offset < 0) {
            throw new BillingServiceException(
                "Offset must be greater than or equal to 0", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Convert offset (page number) to actual page for PageRequest
        // offset=0 → page 0, offset=1 → page 1, offset=2 → page 2
        return commissionRateRepository.findAll(PageRequest.of(offset, limit)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count total commission rates.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @return
     */
    @Override
    public long countCommissionRates() {
        return commissionRateRepository.count();
    }

    /**
     * Delete a commission rate by client ID.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param clientId
     */
    @Override
    public void deleteCommissionRate(String clientId) {
        CommissionRate rate = commissionRateRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission rate not found for client: " + clientId));

        commissionRateRepository.delete(rate);
    }

    public void applyCommissionByTier(String clientId, Double invoiceAmount, String invoiceId) {
        CommissionRate rate = commissionRateRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("No commission config found for client: " + clientId));
    }

    /**
     * Apply commission if eligible based on invoice amount.
     * @author Mahadi Hasan Joy
     * @since  2025-04-23
     * @param clientId
     * @param invoiceAmount
     * @param invoiceId
     */
    @Override
    public void applyCommissionIfEligible(String clientId, Double invoiceAmount, String invoiceId) {
        // Commission is optional - if no config exists, simply return without error
        Optional<CommissionRate> optionalRate = commissionRateRepository.findByClientId(clientId);
        if (optionalRate.isEmpty()) {
            log.debug("No commission config found for client: {}. Skipping commission calculation.", clientId);
            return;
        }

        CommissionRate rate = optionalRate.get();
        if (!rate.isActive()) return;
        if (rate.getMinInvoiceAmount() != null && invoiceAmount < rate.getMinInvoiceAmount()) return;

        double commissionValue = (invoiceAmount * rate.getCommissionPercentage()) / 100.0;

        // ✅ Now recharge the client’s credit account
        Credit credit = creditRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    Credit c = new Credit();
                    c.setClientId(clientId);
                    c.setAvailableAmount(0.0);
                    c.setActive(true);
                    c.setCreatedAt(Instant.now());
                    return creditRepository.save(c);
                });

        credit.setAvailableAmount(credit.getAvailableAmount() + commissionValue);
        credit.setUpdatedAt(Instant.now());
        creditRepository.save(credit);

        // ✅ Log the credit addition
        CreditTransactionCreateDTO tx = new CreditTransactionCreateDTO();
        tx.setClientId(clientId);
        tx.setType("DEPOSIT");
        tx.setAmount(commissionValue);
        tx.setReferenceType("COMMISSION");
        tx.setReferenceId(invoiceId);
        tx.setDescription("Commission earned for Invoice " + invoiceId);
        tx.setInitiatedBy("system");

        creditService.logCreditTransaction(tx);
    }



    private CommissionRate mapToModel(CommissionRateRequestDTO dto) {
        CommissionRate rate = new CommissionRate();
        rate.setClientId(dto.getClientId());
        rate.setMspId(dto.getMspId());
        rate.setCommissionPercentage(dto.getCommissionPercentage());
        rate.setEligibilityCriteria(dto.getEligibilityCriteria());
        rate.setMinInvoiceAmount(dto.getMinInvoiceAmount());
        rate.setActive(true);
        return rate;
    }

    private CommissionRateResponseDTO mapToResponse(CommissionRate rate) {
        return new CommissionRateResponseDTO(
                rate.getId(),
                rate.getClientId(),
                rate.getMspId(),
                rate.getCommissionPercentage(),
                rate.getEligibilityCriteria(),
                rate.getMinInvoiceAmount(),
                rate.isActive(),
                rate.getCreatedAt(),
                rate.getUpdatedAt()
        );
    }
}
