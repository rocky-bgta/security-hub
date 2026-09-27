package com.aspire.asat.cms.dto.packageDto;

import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RequestDto {

    @NotBlank(message = "Package name cannot be blank")
    private String packageName;
    private String packageDescription;
    @Positive(message = "Price must be greater than zero")
    private double price;
    @NotNull(message = "Package status cannot be null")
    private PackageStatus packageStatus;
    private List<String> courseIds;
    private List<String> featureIds;
    private String thumbnailUrl;
    private List<BundleDto> bundles;
    private PackageExamCreateDto exam;


}
