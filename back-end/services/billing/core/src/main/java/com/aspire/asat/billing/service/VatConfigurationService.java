package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.VatConfigurationCreateDTO;
import com.aspire.asat.billing.dto.VatConfigurationResponseDTO;

import java.util.List;

public interface VatConfigurationService {

    VatConfigurationResponseDTO createVatConfiguration(VatConfigurationCreateDTO config);

    VatConfigurationResponseDTO updateVatConfiguration(String countryId, VatConfigurationCreateDTO config);

    VatConfigurationResponseDTO getVatByCountryId(String countryId);

    List<VatConfigurationResponseDTO> getAllVatConfigurations(int offset, int limit);

    long countVatConfigurations();

    void deleteVatConfiguration(String countryId);

    double getVatRate(String id, String stateId);

    double getVatRateForRegion(String countryId, String regionId);

}
