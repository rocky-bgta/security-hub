package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.LlmEscalationLimit;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.enums.VishingScenarioStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "vishing_scenarios")
@CompoundIndex(name = "client_status_idx", def = "{'clientId': 1, 'status': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingScenario {

    @Id
    private String id;

    @Indexed
    @NotBlank
    private String clientId;

    @NotBlank
    @Size(max = 150)
    private String scenarioName;

    private String description;

    private String attackTemplateId;

    private String attackTemplateName;

    private String language;

    private String tone;

    private String role;

    @NotBlank
    private String scriptBody;

    @Builder.Default
    private List<String> detectedVariables = new ArrayList<>();

    @Builder.Default
    private VishingScenarioStatus status = VishingScenarioStatus.DRAFT;

    @Builder.Default
    private boolean isGlobal = false;

    @Builder.Default
    private boolean enableLlmResponses = true;

    /** How the target is expected to respond during the call (DTMF/SPEECH/BOTH). */
    @Builder.Default
    private VishingInteractionMode interactionMode = VishingInteractionMode.BOTH;

    private LlmEscalationLimit escalationLimit;

    @Builder.Default
    private int popularity = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;

    private String lastModifiedBy;
}
