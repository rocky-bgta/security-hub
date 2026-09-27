package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.LearningMode;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.enums.VoiceResponseStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Request DTO for Step 1: Campaign Setup
 * Creates a new campaign with name and type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreateRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 50, message = "Campaign name cannot exceed 50 characters")
    private String campaignName;

    @NotNull(message = "Campaign type is required")
    private CampaignType campaignType;

    /**
     * Optional ClientProduct assignment id ({@code ClientProduct.id}) for license
     * accounting. Required before launch when training data does not supply it.
     */
    private String productPackageId;

  /** Optional; omitted by legacy email clients and defaults to EMAIL. */
    @Builder.Default
    private CampaignChannel channel = CampaignChannel.EMAIL;

    /** Direct assignment policy (email/SMS legacy). For voice, use responseStages instead. */
    private SubPackageAssignedFor assignedFor;

    /** Vishing: response stages (CLICK = answered, COMPROMISED = sensitive data revealed). */
    private List<VoiceResponseStage> responseStages;

    private LearningMode learningMode;

    @NotNull(message = "Expire date is required")
    @Valid
    private CampaignExpireDateRequest expireDate;
}
