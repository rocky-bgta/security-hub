package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = WebApiUrlConstants.NOTIFICATION_API, produces = "application/json")
public interface EmailController {
    @PostMapping
    ResponseEntity<String> sendInvitationEmail(@RequestParam("token") String token);
}
