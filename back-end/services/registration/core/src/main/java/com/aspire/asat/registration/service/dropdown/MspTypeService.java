package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.MspTypeRequestDto;
import com.aspire.asat.registration.data.dropdown.MspTypeRespDto;

import java.util.List;

public interface MspTypeService {

    MspTypeRespDto createMspType(MspTypeRequestDto request);

    MspTypeRespDto getMspTypeById(String id);

    MspTypeRespDto updateMspType(String id, MspTypeRequestDto request);

    List<MspTypeRespDto> getActiveMspTypes();

    void deleteMspType(String id);
}
