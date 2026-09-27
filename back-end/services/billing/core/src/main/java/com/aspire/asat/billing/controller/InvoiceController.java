package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.INVOICE_API, produces = "application/json")
@Tag(name = "Invoice Management", description = "Endpoints for Invoice Management")
public interface InvoiceController {

    @Operation(summary = "Create a new invoice", description = "Generates a new invoice based on selected packages and client data.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invoice data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(WebApiUrlConstants.CREATE)
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> createInvoice(@Valid @RequestBody InvoiceRequestDTO requestDTO);

    @Operation(summary = "Update an existing invoice", description = "Updates invoice details by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invoice ID or data"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{id}")
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> updateInvoice(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody InvoiceRequestDTO requestDTO);

    @Operation(summary = "Update invoice payment status and payment details", 
              description = "Updates payment status and completed payment details for an invoice by ID. " +
                           "Can update payment status to PENDING or COMPLETED, and/or update payment method and details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice payment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invoice ID or payment data"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update-payment/{id}")
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> updateInvoicePayment(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody InvoiceUpdateRequestDto requestDTO);

    @Operation(summary = "Get invoice by ID", description = "Retrieves an invoice by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invoice ID"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/id/{id}")
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> getInvoiceById(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "List invoices", description = "Returns a paginated list of invoices. Supports optional filtering by productId (catalog product in productSelections).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoices listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or client ID"),
            @ApiResponse(responseCode = "404", description = "No invoices found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InvoiceResponseDTO>>>> getInvoices(
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "status", required = false) List<InvoiceStatus> statuses,
            @RequestParam(value = "roleType", required = false) RoleType roleType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by CMS catalog product ID (matches productSelections.productId)")
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Offset must be >= 0") int offset,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be >= 1") int limit
    );


    @Operation(summary = "Delete an invoice", description = "Deletes an invoice by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invoice ID"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteInvoice(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Download invoice PDF", description = "Downloads an invoice as PDF file by invoice ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice PDF downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Failed to generate PDF")
    })
    @GetMapping("/download/{id}")
    ResponseEntity<org.springframework.core.io.Resource> downloadInvoicePdf(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Download invoice CSV", description = "Downloads an invoice as CSV file by invoice ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice CSV downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/download-csv/{id}")
    ResponseEntity<org.springframework.core.io.Resource> downloadInvoiceCsv(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Send invoice email", description = "Sends invoice PDF as email attachment to the client admin email address.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Client admin email not found"),
            @ApiResponse(responseCode = "500", description = "Failed to send email")
    })
    @PostMapping("/send-email/{id}")
    ResponseEntity<ApiResponseDto<String>> sendInvoiceEmail(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Apply coupon to invoice", description = "Validates, calculates, and applies a coupon to an unpaid invoice before payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon applied successfully"),
            @ApiResponse(responseCode = "400", description = "Invoice not eligible or invalid coupon"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "409", description = "Invoice already has a coupon"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/apply-coupon")
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> applyCoupon(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody ApplyCouponRequestDTO request);

    @Operation(summary = "Apply discount to invoice", description = "Applies a one-time invoice-level discount on an unpaid invoice. SUPER_ADMIN, ASPIRE_ADMIN, or SYSTEM_USER only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discount applied successfully"),
            @ApiResponse(responseCode = "400", description = "Invoice not eligible or invalid discount data"),
            @ApiResponse(responseCode = "403", description = "Caller not authorized"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "409", description = "Invoice already has a discount"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/apply-discount")
    ResponseEntity<ApiResponseDto<InvoiceResponseDTO>> applyDiscount(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody ApplyDiscountRequestDTO request);

    @Operation(summary = "Export invoice history as CSV", description = "Generates a CSV file of all filtered invoice history and returns it as a download. Supports the same filters as the list endpoint, including productId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/history/export/csv")
    ResponseEntity<org.springframework.core.io.Resource> downloadInvoiceHistoryCsv(
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "status", required = false) List<InvoiceStatus> statuses,
            @RequestParam(value = "roleType", required = false) RoleType roleType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by CMS catalog product ID (matches productSelections.productId)")
            @RequestParam(value = "productId", required = false) String productId
    );

    @Operation(
            summary = "Check if client admin has only PENDING invoices",
            description = "Returns true if the client admin has no PAID invoices (all invoices are PENDING, or has FAILED/CANCELLED, or no invoices exist). Returns false if at least one PAID invoice exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid clientAdminId"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/check-pending-only/{clientAdminId}")
    ResponseEntity<ApiResponseDto<Boolean>> checkPendingOnly(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId);

    @Operation(
            summary = "Get invoice summary report counts",
            description = "Returns total, paid, unpaid (PENDING), cancelled, and ON_PROGRESS invoice counts. "
                    + "Supports optional filters: search, clientAdminId, mspId, status, startDate, endDate, quickRange. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary counts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary-report/summary")
    ResponseEntity<ApiResponseDto<InvoiceSummaryReportDTO>> getInvoiceSummaryReport(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<InvoiceStatus> statuses,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange
    );

    @Operation(
            summary = "Get invoice summary report detail list",
            description = "Returns a paginated list of invoices for the summary report. "
                    + "Each item is a full InvoiceResponseDTO including productSelections (product/package names). "
                    + "Supports optional filters: search, clientAdminId, mspId, status, startDate, endDate, quickRange. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice detail list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary-report/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InvoiceResponseDTO>>>> getInvoiceSummaryReportList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<InvoiceStatus> statuses,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Offset must be >= 0") int offset,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Limit must be >= 1") int limit
    );

    @Operation(
            summary = "Export invoice summary report as CSV",
            description = "Generates a CSV of filtered invoices for the summary report and returns it as a download. "
                    + "Supports the same optional filters as the summary and list endpoints. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/summary-report/export/csv")
    ResponseEntity<org.springframework.core.io.Resource> downloadInvoiceSummaryReportCsv(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<InvoiceStatus> statuses,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange
    );
}
