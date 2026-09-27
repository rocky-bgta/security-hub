package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.InterfaceType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating/updating sender profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfileRequest {

    @NotBlank(message = "Profile name is required")
    @Size(max = 100, message = "Profile name cannot exceed 100 characters")
    private String profileName;

    @NotNull(message = "Interface type is required")
    private InterfaceType interfaceType;

    @NotBlank(message = "From address is required")
    @Email(message = "Please enter a valid email address")
    private String fromAddress;

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @NotBlank(message = "SMTP host is required")
    private String host;

    @NotNull(message = "Port is required")
    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private Integer port;

    private String username;

    private String password;

    @Builder.Default
    private boolean ignoreCertificateErrors = false;

    @Builder.Default
    private boolean useTls = true;

    private String category;

    private String targetIndustryId;

    private String regionId;

    private String language;

    private DeceptionLevelDto deceptionLevel;

    private List<String> psychologicalTriggers;

    private DomainType domainType;

    private String domainName;

    private PersonalizationLevelDto personalizationLevel;

    private ProviderType providerType;

    private List<String> tags;

    @Email(message = "Please enter a valid reply-to email address")
    private String replyToAddress;
}
