package com.aspire.asat.vps.dto.interactive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentData {
    private String id;
    private CommonContent common;
    private SpecificContent specific;
    private String createdAt;
    private String updatedAt;
}
