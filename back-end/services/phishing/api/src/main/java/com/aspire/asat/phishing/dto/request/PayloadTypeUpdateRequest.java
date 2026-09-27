package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for full replace (PUT) of a payload type entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadTypeUpdateRequest {

    @NotBlank(message = "Payload type name is required")
    @Size(max = 100, message = "Payload type name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    @NotNull(message = "Default flag is required")
    private Boolean isDefault;

    @NotNull(message = "Active flag is required")
    private Boolean isActive;

    @Builder.Default
    private PayloadTypeChannel channel = PayloadTypeChannel.EMAIL;
}
