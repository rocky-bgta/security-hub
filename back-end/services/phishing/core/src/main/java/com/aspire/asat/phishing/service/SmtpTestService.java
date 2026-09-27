package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.model.SenderProfile;

/**
 * Service interface for testing SMTP connections.
 */
public interface SmtpTestService {

    /**
     * Test SMTP connection using an existing profile
     * 
     * @param profile The sender profile to test
     * @return Test result with success status and details
     */
    TestResultDto testConnection(SenderProfile profile);

    /**
     * Test SMTP connection using request parameters (before saving)
     * 
     * @param request The sender profile request with connection details
     * @return Test result with success status and details
     */
    TestResultDto testConnection(SenderProfileRequest request);

    /**
     * Test SMTP connection using raw parameters
     * 
     * @param host SMTP host
     * @param port SMTP port
     * @param username SMTP username
     * @param password SMTP password
     * @param useTls Whether to use TLS
     * @param ignoreCertErrors Whether to ignore certificate errors
     * @return Test result with success status and details
     */
    TestResultDto testConnection(String host, int port, String username, String password,
                                  boolean useTls, boolean ignoreCertErrors);
}
