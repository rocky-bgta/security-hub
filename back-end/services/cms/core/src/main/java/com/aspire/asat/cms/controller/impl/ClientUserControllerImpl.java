package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ClientUserControllerImpl {

    private final ClientUserOperationService clientUserOperationService;

    @Autowired
    public ClientUserControllerImpl(ClientUserOperationService clientUserOperationService) {
        this.clientUserOperationService = clientUserOperationService;
    }

    // All API methods have been moved to specialized controllers:
    // - Dashboard and User APIs -> UserController
    // - Course APIs -> ClientCourseController  
    // - Package APIs -> ClientPackageController
    // - Content APIs -> ClientContentController
    // - Certificate APIs -> CertificateController
    // - Topic APIs -> ClientTopicController
    // - Exam APIs -> ExamController
    //
    // The ClientUserService still contains all the business logic methods
    // which are now called by the specialized service implementations.

}