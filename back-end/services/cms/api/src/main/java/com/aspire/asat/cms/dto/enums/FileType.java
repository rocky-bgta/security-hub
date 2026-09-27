package com.aspire.asat.cms.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {
    CONTENT("CONTENT"),
    INVOICE("INVOICE");
    private final String fileType;
}
