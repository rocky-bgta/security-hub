package com.aspire.asat.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {
    CONTENT("CONTENT"),
    INVOICE("INVOICE"),
    LOGO("LOGO"),
    PDF("PDF"),
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    DOCUMENT("DOCUMENT"),
    IMAGE("IMAGE"),
    THUMBNAIL("THUMBNAIL"),
    ;
    private final String fileType;
}
