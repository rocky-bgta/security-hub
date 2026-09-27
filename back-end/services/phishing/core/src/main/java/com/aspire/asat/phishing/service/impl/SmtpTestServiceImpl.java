package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.service.SmtpTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Locale;

/**
 * Service implementation for testing SMTP connections.
 * Tests SMTP connectivity, STARTTLS/SMTPS negotiation, and AUTH LOGIN authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpTestServiceImpl implements SmtpTestService {

    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    @Override
    public TestResultDto testConnection(SenderProfile profile) {
        return testConnection(
                profile.getHost(),
                profile.getPort(),
                profile.getUsername(),
                profile.getPassword(),
                profile.isUseTls(),
                profile.isIgnoreCertificateErrors()
        );
    }

    @Override
    public TestResultDto testConnection(SenderProfileRequest request) {
        return testConnection(
                request.getHost(),
                request.getPort(),
                request.getUsername(),
                request.getPassword(),
                request.isUseTls(),
                request.isIgnoreCertificateErrors()
        );
    }

    @Override
    public TestResultDto testConnection(String host, int port, String username, String password,
                                        boolean useTls, boolean ignoreCertErrors) {
        long startTime = System.currentTimeMillis();

        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            log.info("Testing SMTP connection to {}:{} with TLS enabled: {}", host, port, useTls);

            // Create initial connection
            if (useTls && port == 465) {
                // Implicit TLS / SMTPS
                SSLSocketFactory factory = getSSLSocketFactory(ignoreCertErrors);
                SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, port);
                sslSocket.setSoTimeout(READ_TIMEOUT_MS);
                sslSocket.startHandshake();
                socket = sslSocket;
            } else {
                // Plain connection for SMTP / STARTTLS
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT_MS);
                socket.setSoTimeout(READ_TIMEOUT_MS);
            }

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // Read server greeting
            String greeting = readSingleLineResponse(reader);
            log.debug("SMTP greeting: {}", greeting);

            if (greeting == null || !greeting.startsWith("220")) {
                return buildFailureResult(startTime, "Invalid server greeting", greeting);
            }

            // Initial EHLO
            sendCommand(writer, "EHLO localhost");
            String ehloResponse = readMultiLineResponse(reader);
            log.debug("EHLO response: {}", ehloResponse);

            if (!hasResponseCode(ehloResponse, "250")) {
                return buildFailureResult(startTime, "EHLO command failed", ehloResponse);
            }

            // STARTTLS for port 587
            if (useTls && port == 587) {
                if (!containsCapability(ehloResponse, "STARTTLS")) {
                    return buildFailureResult(startTime,
                            "TLS is enabled but the server does not advertise STARTTLS",
                            ehloResponse);
                }

                sendCommand(writer, "STARTTLS");
                String startTlsResponse = readSingleLineResponse(reader);
                log.debug("STARTTLS response: {}", startTlsResponse);

                if (startTlsResponse == null || !startTlsResponse.startsWith("220")) {
                    return buildFailureResult(startTime, "STARTTLS command failed", startTlsResponse);
                }

                SSLSocketFactory sslFactory = getSSLSocketFactory(ignoreCertErrors);
                SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(socket, host, port, true);
                sslSocket.setSoTimeout(READ_TIMEOUT_MS);
                sslSocket.startHandshake();

                // Promote upgraded TLS socket
                socket = sslSocket;

                // Recreate streams after TLS upgrade
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

                // EHLO again after STARTTLS
                sendCommand(writer, "EHLO localhost");
                ehloResponse = readMultiLineResponse(reader);
                log.debug("EHLO after STARTTLS: {}", ehloResponse);

                if (!hasResponseCode(ehloResponse, "250")) {
                    return buildFailureResult(startTime, "EHLO after STARTTLS failed", ehloResponse);
                }
            }

            // Authentication
            if (!containsCapability(ehloResponse, "AUTH")) {
                long responseTime = System.currentTimeMillis() - startTime;
                return TestResultDto.builder()
                        .success(true)
                        .message("SMTP connection successful, but server does not advertise AUTH")
                        .responseTimeMs(responseTime)
                        .serverResponse(ehloResponse)
                        .build();
            }

            if (!supportsAuthLogin(ehloResponse)) {
                long responseTime = System.currentTimeMillis() - startTime;
                return TestResultDto.builder()
                        .success(true)
                        .message("SMTP connection successful, but AUTH LOGIN is not supported by the server")
                        .responseTimeMs(responseTime)
                        .serverResponse(ehloResponse)
                        .build();
            }

            sendCommand(writer, "AUTH LOGIN");
            String authResponse = readSingleLineResponse(reader);
            log.debug("AUTH LOGIN response: {}", authResponse);

            if (authResponse == null || !authResponse.startsWith("334")) {
                return buildFailureResult(startTime, "AUTH LOGIN command rejected", authResponse);
            }

            String encodedUsername = Base64.getEncoder()
                    .encodeToString(username.getBytes(StandardCharsets.UTF_8));
            sendCommand(writer, encodedUsername);

            String userResponse = readSingleLineResponse(reader);
            log.debug("Username response: {}", userResponse);

            if (userResponse == null || !userResponse.startsWith("334")) {
                return buildFailureResult(startTime, "Username was not accepted", userResponse);
            }

            String encodedPassword = Base64.getEncoder()
                    .encodeToString(password.getBytes(StandardCharsets.UTF_8));
            sendCommand(writer, encodedPassword);

            String passResponse = readSingleLineResponse(reader);
            log.debug("Password response: {}", passResponse);

            if (passResponse != null && passResponse.startsWith("235")) {
                long responseTime = System.currentTimeMillis() - startTime;
                return TestResultDto.builder()
                        .success(true)
                        .message("SMTP connection and authentication successful")
                        .responseTimeMs(responseTime)
                        .serverResponse(passResponse)
                        .build();
            }

            return buildFailureResult(startTime, "Authentication failed", passResponse);

        } catch (SocketTimeoutException e) {
            log.error("SMTP connection test timed out: {}", e.getMessage(), e);
            return buildFailureResult(startTime, "Connection timed out: " + e.getMessage(), null);
        } catch (SSLException e) {
            log.error("SMTP TLS/SSL error: {}", e.getMessage(), e);
            return buildFailureResult(startTime, "TLS/SSL error: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("SMTP connection test failed: {}", e.getMessage(), e);
            return buildFailureResult(startTime, "Connection failed: " + e.getMessage(), null);
        } finally {
            try {
                if (writer != null) {
                    try {
                        sendCommand(writer, "QUIT");
                    } catch (Exception ignored) {
                        // Ignore QUIT failures during cleanup
                    }
                }
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception ignored) {
                }

                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception ignored) {
                }

                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private SSLSocketFactory getSSLSocketFactory(boolean ignoreCertErrors) throws Exception {
        if (ignoreCertErrors) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        }

        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    private void sendCommand(BufferedWriter writer, String command) throws Exception {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
        log.debug("SMTP C: {}", command);
    }

    private String readSingleLineResponse(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        log.debug("SMTP S: {}", line);
        return line;
    }

    private String readMultiLineResponse(BufferedReader reader) throws Exception {
        StringBuilder response = new StringBuilder();

        String firstLine = reader.readLine();
        if (firstLine == null) {
            return null;
        }

        response.append(firstLine).append("\n");
        log.debug("SMTP S: {}", firstLine);

        if (firstLine.length() < 4) {
            return response.toString().trim();
        }

        String code = firstLine.substring(0, 3);

        // Multi-line SMTP format:
        // 250-first line
        // 250-second line
        // 250 last line
        if (firstLine.charAt(3) == '-') {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                log.debug("SMTP S: {}", line);

                if (line.length() >= 4 && line.startsWith(code) && line.charAt(3) == ' ') {
                    break;
                }
            }
        }

        return response.toString().trim();
    }

    private boolean hasResponseCode(String response, String code) {
        if (response == null || response.isBlank()) {
            return false;
        }

        String[] lines = response.split("\\R");
        for (String line : lines) {
            if (line.startsWith(code)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCapability(String ehloResponse, String capability) {
        return ehloResponse != null
                && ehloResponse.toUpperCase(Locale.ROOT).contains(capability.toUpperCase(Locale.ROOT));
    }

    private boolean supportsAuthLogin(String ehloResponse) {
        if (ehloResponse == null) {
            return false;
        }

        String upper = ehloResponse.toUpperCase(Locale.ROOT);
        return upper.contains("AUTH LOGIN") || upper.contains("AUTH=LOGIN");
    }

    private TestResultDto buildFailureResult(long startTime, String message, String serverResponse) {
        return TestResultDto.builder()
                .success(false)
                .message(message)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .serverResponse(serverResponse)
                .build();
    }
}