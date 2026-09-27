package com.aspire.asat.cms.config;

import com.aspire.asat.cms.dto.enums.TimeFrame;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to convert string request parameter to TimeFrame enum
 * Handles case-insensitive conversion
 */
@Component
public class TimeFrameConverter implements Converter<String, TimeFrame> {

    @Override
    public TimeFrame convert(String source) {
        return TimeFrame.fromString(source);
    }
}

