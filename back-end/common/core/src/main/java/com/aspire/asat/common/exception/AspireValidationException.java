package com.aspire.asat.common.exception;

import java.util.List;

public interface AspireValidationException {
    List<? extends AspireValidationException> getAllValidationException();
}
