package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.InvoiceSummaryReportDTO;
import com.aspire.asat.billing.dto.invoice.ApplyCouponRequestDTO;
import com.aspire.asat.billing.dto.invoice.ApplyDiscountRequestDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.InvoiceRequestDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceResponseDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceUpdateRequestDto;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;

import java.util.List;

public interface InvoiceService {


    InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO);

    InvoiceResponseDTO updateInvoice(String id, InvoiceRequestDTO requestDTO);

    InvoiceResponseDTO updateInvoicePayment(String invoiceId, InvoiceUpdateRequestDto requestDTO);

    InvoiceResponseDTO getInvoiceById(String id);

    List<InvoiceResponseDTO> getInvoicesByClientId(String clientId, int offset, int limit);

    List<InvoiceResponseDTO> getInvoices(String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId, int offset, int limit);

    long countInvoices(String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId);

    InvoiceResponseDTO applyCouponToInvoice(String invoiceId, ApplyCouponRequestDTO request);

    InvoiceResponseDTO applyDiscountToInvoice(String invoiceId, ApplyDiscountRequestDTO request);

    void deleteInvoice(String id);

    byte[] downloadInvoicePdf(String invoiceId);

    byte[] downloadInvoiceCsv(String invoiceId);

    void sendInvoiceEmail(String invoiceId);

    byte[] generateInvoiceHistoryCsv(String clientId, String mspId, String countryId,
        List<InvoiceStatus> statuses, RoleType roleType, String startDate, String endDate, String search, String productId);

    boolean hasOnlyPendingInvoices(String clientAdminId);

    InvoiceSummaryReportDTO getInvoiceSummaryReport(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange);

    List<InvoiceResponseDTO> getInvoiceSummaryReportList(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange, int offset, int limit);

    long countInvoiceSummaryReport(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange);

    byte[] generateInvoiceSummaryReportCsv(String search, String clientAdminId, String mspId,
            List<InvoiceStatus> statuses, String startDate, String endDate, QuickRange quickRange);
}
