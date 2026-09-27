package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Template definition for confirmation or transactional emails")
public class EmailTemplateDto {

    @NotBlank
    @Schema(
            description = "Type of email template (e.g., PAYMENT_CONFIRMATION, WELCOME, etc.)",
            example = "PAYMENT_CONFIRMATION",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String templateType;

    @NotBlank
    @Schema(
            description = "Email body with supported placeholders (e.g., {{CLIENT_NAME}}, {{INVOICE_ID}})",
            example = """
                Dear {{CLIENT_NAME}},

                Thank you for your payment! Your transaction has been processed successfully.

                Payment Details:
                • Invoice ID: {{INVOICE_ID}}
                • Amount Paid: $99
                • Payment Method: {{PAYMENT_METHOD}}

                Your Login Credentials:
                • Username: {{USERNAME}}
                • Temporary Password: {{TEMP_PASSWORD}}
                • Portal Link: {{PORTAL_LINK}}

                Your account is now active. Please log in using the credentials above and update your password upon first login.

                Best regards,
                The Team
                """,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String templateBody;
}
