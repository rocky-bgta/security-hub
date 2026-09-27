package com.aspire.asat.cms.dto.product;

import com.aspire.asat.cms.dto.enums.ProductStatus;
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

    @NotBlank(message = "Product name cannot be blank")
    private String productName;
    private String productDescription;
    @NotNull(message = "Product status cannot be null")
    private ProductStatus productStatus;
    private List<String> courseIds;
    private String thumbnailUrl;

}
