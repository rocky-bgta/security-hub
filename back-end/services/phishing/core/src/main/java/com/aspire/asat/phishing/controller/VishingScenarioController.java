package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VishingScenarioCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingScenarioUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingScenarioDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vishing Scenarios", description = "APIs for managing vishing scenario scripts")
@RequestMapping(value = WebApiUrlConstants.VISHING_SCENARIOS_PATH)
public interface VishingScenarioController {

    @Operation(summary = "List vishing scenarios")
    @GetMapping
    ResponseEntity<AllResponseDto<List<VishingScenarioDto>>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String searchParam);

    @Operation(summary = "Get vishing scenario by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<VishingScenarioDto>> getById(@PathVariable String id);

    @Operation(summary = "Create vishing scenario")
    @PostMapping
    ResponseEntity<ApiResponseDto<VishingScenarioDto>> create(
            @Valid @RequestBody VishingScenarioCreateRequest request);

    @Operation(summary = "Update vishing scenario")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<VishingScenarioDto>> update(
            @PathVariable String id,
            @Valid @RequestBody VishingScenarioUpdateRequest request);

    @Operation(summary = "Delete vishing scenario")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id);

    @Operation(summary = "Publish vishing scenario")
    @PostMapping("/{id}/publish")
    ResponseEntity<ApiResponseDto<VishingScenarioDto>> publish(@PathVariable String id);
}
