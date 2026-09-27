package com.aspire.asat.cms.dto.featureDto;

import com.aspire.asat.cms.dto.enums.Availability;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RequestDto {

    @NotBlank(message = "Feature name cannot be blank")
    private String featureName;
    private String featureDescription;
    @NotNull(message = "Feature status cannot be null")
    private FeatureStatus featureStatus;
    private List<String> packageIds;
    private Availability availability;
    private String thumbnailUrl;

}
