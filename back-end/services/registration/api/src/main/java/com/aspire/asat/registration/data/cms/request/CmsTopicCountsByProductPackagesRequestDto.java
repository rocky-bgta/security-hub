package com.aspire.asat.registration.data.cms.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to count topics for MSP and client product/package pairs via CMS")
public class CmsTopicCountsByProductPackagesRequestDto {

    private List<CmsProductPackagePairDto> mspProductPackages;
    private List<CmsProductPackagePairDto> clientProductPackages;
}
