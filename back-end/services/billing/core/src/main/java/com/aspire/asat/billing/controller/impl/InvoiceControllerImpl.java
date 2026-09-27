package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.InvoiceController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.InvoiceSummaryReportDTO;
import com.aspire.asat.billing.dto.invoice.ApplyCouponRequestDTO;
import com.aspire.asat.billing.dto.invoice.ApplyDiscountRequestDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.InvoiceRequestDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceResponseDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceUpdateRequestDto;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.service.InvoiceService;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class InvoiceControllerImpl implements InvoiceController {

    private final InvoiceService invoiceService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created invoice for client admin: #{#requestDTO.clientAdminId != null ? #requestDTO.clientAdminId : 'N/A'}",
            clientAdminIdExpression = "#{#requestDTO.clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> createInvoice(InvoiceRequestDTO requestDTO) {
        log.info("Creating invoice for client admin: {}", requestDTO.getClientAdminId());
        InvoiceResponseDTO response = invoiceService.createInvoice(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Invoice created successfully", 201, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated invoice: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.clientAdminId != null ? #requestDTO.clientAdminId : #id}",
            clientAdminIdExpression = "#{#requestDTO.clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> updateInvoice(String id, InvoiceRequestDTO requestDTO) {
        log.info("Updating invoice: {}", id);
        InvoiceResponseDTO response = invoiceService.updateInvoice(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice updated successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated invoice payment for invoice: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.paymentStatus != null ? #requestDTO.paymentStatus.toString() : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> updateInvoicePayment(String id, InvoiceUpdateRequestDto requestDTO) {
        log.info("Updating invoice payment for invoice ID: {}, paymentStatus: {}", id, 
                requestDTO.getPaymentStatus() != null ? requestDTO.getPaymentStatus() : "not provided");
        InvoiceResponseDTO response = invoiceService.updateInvoicePayment(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice payment updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> getInvoiceById(String id) {
        log.info("Getting invoice by ID: {}", id);
        InvoiceResponseDTO response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InvoiceResponseDTO>>>> getInvoices(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            String startDate, String endDate, String search, String productId, int offset, int limit) {
        log.info("Getting invoices with filters - clientId: {}, mspId: {}, countryId: {}, statuses: {}, roleType: {}, startDate: {}, endDate: {}, search: {}, productId: {}, offset: {}, limit: {}",
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId, offset, limit);

        List<InvoiceResponseDTO> invoices = invoiceService.getInvoices(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId, offset, limit);
        long total = invoiceService.countInvoices(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);
        
        AllResponseDto<List<InvoiceResponseDTO>> response = new AllResponseDto<>(offset, limit, total, invoices);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoices retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteInvoice(String id) {
        log.info("Deleting invoice: {}", id);
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<Resource> downloadInvoicePdf(String id) {
        log.info("Downloading invoice PDF for invoice ID: {}", id);
        
        try {
            // Generate PDF bytes
            byte[] pdfBytes = invoiceService.downloadInvoicePdf(id);
            
            // Create resource from byte array
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            // Set filename
            String filename = "Invoice_" + id + ".pdf";
            
            // Return PDF as downloadable file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading invoice PDF for ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Resource> downloadInvoiceCsv(String id) {
        log.info("Downloading invoice CSV for invoice ID: {}", id);
        
        try {
            // Generate CSV bytes
            byte[] csvBytes = invoiceService.downloadInvoiceCsv(id);
            
            // Create resource from byte array
            ByteArrayResource resource = new ByteArrayResource(csvBytes);
            
            // Set filename
            String filename = "Invoice_" + id + ".csv";
            
            // Return CSV as downloadable file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .contentLength(csvBytes.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading invoice CSV for ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Applied coupon to invoice: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#request.couponCode}"
    )
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> applyCoupon(String id, ApplyCouponRequestDTO request) {
        log.info("Applying coupon {} to invoice {}", request.getCouponCode(), id);
        InvoiceResponseDTO response = invoiceService.applyCouponToInvoice(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Coupon applied successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Applied discount to invoice: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#request.discountType}"
    )
    public ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> applyDiscount(String id, ApplyDiscountRequestDTO request) {
        log.info("Applying {} discount to invoice {}", request.getDiscountType(), id);
        InvoiceResponseDTO response = invoiceService.applyDiscountToInvoice(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Discount applied successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> sendInvoiceEmail(String id) {
        log.info("Sending invoice email for invoice ID: {}", id);
        
        try {
            invoiceService.sendInvoiceEmail(id);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Invoice email sent successfully", 200, "Email sent to client admin")
            );
        } catch (Exception e) {
            log.error("Error sending invoice email for ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Resource> downloadInvoiceHistoryCsv(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses,
            RoleType roleType, String startDate, String endDate, String search, String productId) {
        log.info("Generating invoice history CSV with filters - clientId: {}, mspId: {}, countryId: {}, statuses: {}, roleType: {}, startDate: {}, endDate: {}, search: {}, productId: {}",
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);

        byte[] csvData = invoiceService.generateInvoiceHistoryCsv(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);
        
        ByteArrayResource resource = new ByteArrayResource(csvData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-history-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> checkPendingOnly(String clientAdminId) {
        log.info("Checking if client admin {} has only PENDING invoices", clientAdminId);
        boolean hasOnlyPending = invoiceService.hasOnlyPendingInvoices(clientAdminId);
        return ResponseEntity.ok(
                new ApiResponseDto<>("Check completed successfully", 200, hasOnlyPending)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<InvoiceSummaryReportDTO>> getInvoiceSummaryReport(
            String search, String clientAdminId, String mspId, List<InvoiceStatus> statuses,
            String startDate, String endDate, QuickRange quickRange) {
        log.info("Getting invoice summary report - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        InvoiceSummaryReportDTO summary = invoiceService.getInvoiceSummaryReport(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice summary report retrieved successfully", 200, summary));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InvoiceResponseDTO>>>> getInvoiceSummaryReportList(
            String search, String clientAdminId, String mspId, List<InvoiceStatus> statuses,
            String startDate, String endDate, QuickRange quickRange, int offset, int limit) {
        log.info("Getting invoice summary report list - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, startDate: {}, endDate: {}, quickRange: {}, offset: {}, limit: {}",
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange, offset, limit);

        int effectiveLimit = Math.min(limit, 100);

        List<InvoiceResponseDTO> invoices = invoiceService.getInvoiceSummaryReportList(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange, offset, effectiveLimit);
        long total = invoiceService.countInvoiceSummaryReport(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        AllResponseDto<List<InvoiceResponseDTO>> response = new AllResponseDto<>(offset, effectiveLimit, total, invoices);
        return ResponseEntity.ok(new ApiResponseDto<>("Invoice summary report list retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<Resource> downloadInvoiceSummaryReportCsv(
            String search, String clientAdminId, String mspId, List<InvoiceStatus> statuses,
            String startDate, String endDate, QuickRange quickRange) {
        log.info("Exporting invoice summary report CSV - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        byte[] csvData = invoiceService.generateInvoiceSummaryReportCsv(
                search, clientAdminId, mspId, statuses, startDate, endDate, quickRange);

        ByteArrayResource resource = new ByteArrayResource(csvData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-summary-report-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }
}
