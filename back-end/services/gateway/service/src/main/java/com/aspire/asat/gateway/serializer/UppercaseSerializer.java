package com.aspire.asat.gateway.serializer;

import com.fasterxml.jackson.databind.util.StdConverter;


public class UppercaseSerializer extends StdConverter<String, String> {
    @Override
    public String convert(String content) {
        return content != null ? content.toUpperCase() : null;
    }
}
