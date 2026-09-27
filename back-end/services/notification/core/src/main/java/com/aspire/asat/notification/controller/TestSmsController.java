package com.aspire.asat.notification.controller;

import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.SmsTestRequest;
import com.aspire.asat.notification.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = WebApiUrlConstants.NOTIFICATION_API + "/test", produces = "application/json")
public class TestSmsController {

    private final SmsService smsService;

    public TestSmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Test SMS send with JSON body.
     * POST /api/v1/test/sms/send
     */
    @PostMapping("/sms/send")
    public ResponseEntity<ApiResponseDto<String>> sendTestSms(@Valid @RequestBody SmsTestRequest request) {
        try {
            boolean sent = smsService.sendSms(request.getPhoneNumber(), request.getMessage());
            if (sent) {
                return ResponseEntity.ok(new ApiResponseDto<>(
                        "SMS sent successfully",
                        200,
                        "SMS delivered to: " + request.getPhoneNumber()
                ));
            }
            return ResponseEntity.status(500).body(new ApiResponseDto<>(
                    "Failed to send SMS. Check provider configuration and logs.",
                    500,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDto<>(
                    "Failed to send SMS: " + e.getMessage(),
                    500,
                    null
            ));
        }
    }

    /**
     * Quick SMS test with query params.
     * POST /api/v1/test/sms/simple?phoneNumber=+8801...&message=hello
     */
    @PostMapping("/sms/simple")
    public ResponseEntity<ApiResponseDto<String>> sendSimpleTestSms(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String message) {
        try {
            String smsBody = (message != null && !message.isBlank())
                    ? message
                    : "ASAT SMS test message";
            boolean sent = smsService.sendSms(phoneNumber, smsBody);
            if (sent) {
                return ResponseEntity.ok(new ApiResponseDto<>(
                        "Simple SMS sent successfully",
                        200,
                        "SMS delivered to: " + phoneNumber
                ));
            }
            return ResponseEntity.status(500).body(new ApiResponseDto<>(
                    "Failed to send simple SMS. Check provider configuration and logs.",
                    500,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDto<>(
                    "Failed to send simple SMS: " + e.getMessage(),
                    500,
                    null
            ));
        }
    }
}
