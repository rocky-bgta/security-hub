package com.aspire.asat.phishing.service;

import java.io.File;

public interface OpenAiService {
    String transcribe(File file, String languageHint, String correlationId);
}
