package com.aspire.asat.gateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
@Tag(name = "Home", description = "Home Controller for Gateway Service")
public class HomeController {

    @GetMapping("home")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok(
                "Welcome to Aspire ASAT Gateway Service! " +
                        "Please refer to the API documentation for available endpoints."
        );
    }
}
