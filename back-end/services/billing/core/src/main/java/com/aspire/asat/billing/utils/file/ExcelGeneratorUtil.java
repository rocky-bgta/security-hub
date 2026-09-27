package com.aspire.asat.billing.utils.file;

import com.aspire.asat.billing.dto.PaymentHistoryItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
public class ExcelGeneratorUtil {

    public static ByteArrayOutputStream generatePaymentHistoryExcel(List<PaymentHistoryItemDTO> items) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payment History");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"SL No", "Payment ID", "Invoice ID", "Invoice Number", "Client ID", "MSP ID", "Invoice Date", "Payment Date", "Total Amount", "Amount Paid", "Outstanding", "Due Amount", "Status", "Payment Method"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Data rows
            for (int i = 0; i < items.size(); i++) {
                PaymentHistoryItemDTO item = items.get(i);
                Row row = sheet.createRow(i + 1);
                int colIndex = 0;
                row.createCell(colIndex++).setCellValue(i + 1); // SL No
                row.createCell(colIndex++).setCellValue(item.getId() != null ? item.getId() : ""); // Payment ID
                row.createCell(colIndex++).setCellValue(item.getInvoiceId() != null ? item.getInvoiceId() : "");
                row.createCell(colIndex++).setCellValue(item.getInvoiceNumber() != null ? item.getInvoiceNumber() : "");
                row.createCell(colIndex++).setCellValue(item.getClientId() != null ? item.getClientId() : "");
                row.createCell(colIndex++).setCellValue(item.getMspId() != null ? item.getMspId() : "");
                row.createCell(colIndex++).setCellValue(item.getInvoiceDate() != null ? item.getInvoiceDate().toString() : "");
                row.createCell(colIndex++).setCellValue(item.getDate() != null ? item.getDate() : "");
                row.createCell(colIndex++).setCellValue(item.getTotalAmount() != null ? item.getTotalAmount() : 0.0);
                row.createCell(colIndex++).setCellValue(item.getAmount() != null ? item.getAmount() : 0.0);
                row.createCell(colIndex++).setCellValue(item.getOutstanding() != null ? item.getOutstanding() : 0.0);
                row.createCell(colIndex++).setCellValue(item.getDueAmount() != null ? item.getDueAmount() : 0.0);
                row.createCell(colIndex++).setCellValue(item.getStatus() != null ? item.getStatus() : "");
                row.createCell(colIndex++).setCellValue(item.getPaymentMethod() != null ? item.getPaymentMethod() : "");
            }

            workbook.write(out);
            return out;

        } catch (Exception e) {
            log.error("Failed to generate Excel sheet", e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
