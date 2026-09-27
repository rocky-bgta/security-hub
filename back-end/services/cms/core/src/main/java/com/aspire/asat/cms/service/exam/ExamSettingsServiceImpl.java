package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsCreateRequestDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsUpdateRequestDto;
import com.aspire.asat.cms.exception.ExamSettingsNotFoundException;
import com.aspire.asat.cms.exception.ExamSettingsAlreadyExistsException;
import com.aspire.asat.cms.mapper.ExamSettingsMapper;
import com.aspire.asat.cms.model.ExamSettings;
import com.aspire.asat.cms.repository.ExamSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSettingsServiceImpl implements ExamSettingsService {
    private final ExamSettingsRepository examSettingsRepository;
    private final ExamSettingsMapper examSettingsMapper;

    @Override
    public ExamSettingsDto createSettings(ExamSettingsCreateRequestDto request) {
        log.info("Creating client exam settings for client ID: {}", request.getClientId());

        // Check if settings already exist for this client
        if (examSettingsRepository.existsByClientId(request.getClientId())) {
            throw new ExamSettingsAlreadyExistsException("Settings already exist for client: " + request.getClientId());
        }

        // Validate passing score
        if (request.getPassingScore() > 100) {
            throw new IllegalArgumentException("Passing score cannot exceed 100");
        }

        ExamSettings settings = examSettingsMapper.toEntity(request);
        ExamSettings savedSettings = examSettingsRepository.save(settings);

        log.info("Successfully created client exam settings with ID: {}", savedSettings.getId());
        return examSettingsMapper.toDto(savedSettings);
    }

    @Override
    public ExamSettingsDto getSettingsByClientId(String clientId) {
        log.info("Retrieving client exam settings for client ID: {}", clientId);

        Optional<ExamSettings> settings = examSettingsRepository.findByClientIdAndIsActiveTrue(clientId);
        if (settings.isEmpty()) {
            throw new ExamSettingsNotFoundException("No active settings found for client: " + clientId);
        }

        return examSettingsMapper.toDto(settings.get());
    }

    @Override
    public ExamSettingsDto getExamSettingForClient(String clientId) {
        log.info("Retrieving exam settings for client ID: {}", clientId);
        return examSettingsRepository.findByClientId(clientId)
                .or(() -> {
                    log.info("No settings found for client ID: {}. Fetching default settings.", clientId);
                    return examSettingsRepository.findByDefaultSettingsTrueAndIsActiveTrue();
                })
                .map(examSettingsMapper::toDto)
                .orElseThrow(() -> new ExamSettingsNotFoundException("No default settings found"));
    }

    @Override
    public ExamSettingsDto getSettingsById(String id) {
        log.info("Retrieving client exam settings by ID: {}", id);

        return examSettingsRepository.findById(id)
                .map(examSettingsMapper::toDto)
                .orElseThrow(() -> new ExamSettingsNotFoundException("Settings not found with ID: " + id));
    }

    @Override
    public ExamSettingsDto updateSettings(String id, ExamSettingsUpdateRequestDto request) {
        log.info("Updating client exam settings with ID: {}", id);

        Optional<ExamSettings> existingSettings = examSettingsRepository.findById(id);
        if (existingSettings.isEmpty()) {
            throw new ExamSettingsNotFoundException("Settings not found with ID: " + id);
        }

        // Validate passing score if provided
        if (request.getPassingScore() != null && request.getPassingScore() > 100) {
            throw new IllegalArgumentException("Passing score cannot exceed 100");
        }

        ExamSettings updatedSettings = examSettingsMapper.updateEntity(existingSettings.get(), request);
        ExamSettings savedSettings = examSettingsRepository.save(updatedSettings);

        log.info("Successfully updated client exam settings with ID: {}", savedSettings.getId());
        return examSettingsMapper.toDto(savedSettings);
    }

    @Override
    public List<ExamSettingsDto> getAllSettingsByClientId(String clientId) {
        log.info("Retrieving all client exam settings for client ID: {}", clientId);

        List<ExamSettings> settings = examSettingsRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return settings.stream()
                .map(examSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamSettingsDto> getAllActiveSettings() {
        log.info("Retrieving all active client exam settings");

        List<ExamSettings> settings = examSettingsRepository.findByIsActiveTrue();
        return settings.stream()
                .map(examSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExamSettingsDto getDefaultSettings() {
        log.info("Retrieving default client exam settings");

        Optional<ExamSettings> settings = examSettingsRepository.findByDefaultSettingsTrueAndIsActiveTrue();
        if (settings.isEmpty()) {
            throw new ExamSettingsNotFoundException("No default settings found");
        }

        return examSettingsMapper.toDto(settings.get());
    }

    @Override
    public boolean settingsExistForClient(String clientId) {
        return examSettingsRepository.existsByClientId(clientId);
    }
}
