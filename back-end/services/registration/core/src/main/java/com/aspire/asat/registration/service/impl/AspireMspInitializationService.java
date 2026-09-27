package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_CREATED_BY;
import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_MSP_EMAIL;
import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_MSP_ID;
import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_MSP_NAME;
import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_MSP_NOTES;
import static com.aspire.asat.registration.utils.DefaultMspData.DEFAULT_MSP_STATUS;

/**
 * Service responsible for initializing the default Aspire MSP at application startup.
 * This ensures the default MSP exists for buy now onboarding flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AspireMspInitializationService implements CommandLineRunner {

    private final MspUsersRepository mspUsersRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Aspire MSP initialization...");
        initializeAspireMsp();
        log.info("Aspire MSP initialization completed.");
    }

    /**
     * Initialize the default Aspire MSP.
     * Creates the MSP if it doesn't exist, skips if it already exists.
     */
    private void initializeAspireMsp() {
        try {
            // Check if Aspire MSP already exists by organization name
            boolean exists = mspUsersRepository.findAll().stream()
                    .anyMatch(msp -> DEFAULT_MSP_NAME.equalsIgnoreCase(msp.getOrganizationName()));
            
            if (exists) {
                log.info("Default Aspire MSP already exists, skipping initialization.");
                return;
            }

            // Check if MSP with default ID exists
            if (mspUsersRepository.findById(DEFAULT_MSP_ID).isPresent()) {
                log.info("MSP with ID '{}' already exists, skipping initialization.", DEFAULT_MSP_ID);
                return;
            }

            // Create the default Aspire MSP
            Instant now = Instant.now();
            MspUser aspireMsp = MspUser.builder()
                    .id(DEFAULT_MSP_ID)
                    .mspId(DEFAULT_MSP_ID)
                    .organizationName(DEFAULT_MSP_NAME)
                    .contactEmail(DEFAULT_MSP_EMAIL)
                    .mspAdminEmail(DEFAULT_MSP_EMAIL)
                    .status(DEFAULT_MSP_STATUS)
                    .createdBy(DEFAULT_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .notes(DEFAULT_MSP_NOTES)
                    .productIds(new ArrayList<>())
                    .packageIds(new ArrayList<>())
                    .clientProductIds(new ArrayList<>())
                    .roleIds(new ArrayList<>())
                    .build();

            MspUser savedMsp = mspUsersRepository.save(aspireMsp);
            log.info("Successfully initialized default Aspire MSP with ID: {} and name: {}", 
                    savedMsp.getId(), savedMsp.getOrganizationName());

        } catch (Exception e) {
            log.error("Failed to initialize default Aspire MSP", e);
            // Don't throw exception to allow application to continue starting
        }
    }
}

