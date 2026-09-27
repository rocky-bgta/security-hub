package com.aspire.asat.cms.dto.product;

import com.aspire.asat.cms.dto.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreationRequest {

    @NotBlank(message = "Product name cannot be blank")
    private String productName;

    @Size(max = 500, message = "Product description cannot exceed 500 characters")
    private String productDescription;

    @NotNull(message = "Product status cannot be null")
    private ProductStatus productStatus;

    private String thumbnailUrl;

    private List<String> tags; // possible values - Phishing, Security, Compliance, etc.

    @Valid
    @Size(max = 5, message = "Maximum 5 packages allowed per product")
    private List<PackageRequest> packages;

    private Boolean isTrial = false;
    private Boolean showInSite = false;

    private Integer displayOrder;

}
