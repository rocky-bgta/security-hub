package com.aspire.asat.phishing.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal {@code data} payload from CMS sub-package create response (extra fields ignored by Jackson).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmsSubPackageCreatedData {

    private String id;
}
