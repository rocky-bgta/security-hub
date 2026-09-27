package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsCreateRequestDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsUpdateRequestDto;

import java.util.List;

/**
 * Service interface for managing client exam settings
 */
public interface ExamSettingsService {

    /**
     * Create new client exam settings
     */
    ExamSettingsDto createSettings(ExamSettingsCreateRequestDto request);

    /**
     * Get settings by client ID
     */
    ExamSettingsDto getSettingsByClientId(String clientId);

    ExamSettingsDto getExamSettingForClient(String clientId);

    /**
     * Get settings by ID
     */
    ExamSettingsDto getSettingsById(String id);

    /**
     * Update settings by ID
     */
    ExamSettingsDto updateSettings(String id, ExamSettingsUpdateRequestDto request);

    /**
     * Get all settings for a client
     */
    List<ExamSettingsDto> getAllSettingsByClientId(String clientId);

    /**
     * Get all active settings
     */
    List<ExamSettingsDto> getAllActiveSettings();

    /**
     * Get default settings
     */
    ExamSettingsDto getDefaultSettings();


    /**
     * Check if settings exist for a client
     */
    boolean settingsExistForClient(String clientId);
}
