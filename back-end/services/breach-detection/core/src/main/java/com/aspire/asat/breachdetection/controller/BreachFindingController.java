package com.aspire.asat.breachdetection.controller;

import com.aspire.asat.breachdetection.constant.WebApiUrlConstants;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import com.aspire.asat.breachdetection.dto.request.InsecureWebBreachSearchRequest;
import com.aspire.asat.breachdetection.dto.response.BreachActivityResponseDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebBreachFindingDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebFindingsSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "EMAIL Breach Findings", description = "APIs for viewing stored InsecureWeb breach findings")
@RequestMapping(value = WebApiUrlConstants.INSECURE_WEB_API + "/findings")
public interface BreachFindingController {

    @Operation(summary = "List stored breach findings")
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InsecureWebBreachFindingDto>>>> getFindings(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @Schema(description = "Filter date format is: yyyy-MM-dd")
            @RequestParam(required = false) String fromDate,
            @Schema(description = "Filter date format is: yyyy-MM-dd")
            @RequestParam(required = false) String toDate,

            @RequestParam(required = false) String email,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) InsecureWebBreachStatus breachStatus
    );

    @Operation(summary = "Get stored breach finding by id")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<InsecureWebBreachFindingDto>> getFindingById(@PathVariable String id);

    @Operation(
            summary = "Email breach activity timeseries (last 30/60/90 days)",
            description = "Daily counts of email breach findings for the current client, grouped by severity. "
                    + "Allowed values for `days`: 30, 60, 90. Defaults to 30."
    )
    @GetMapping("/activity")
    ResponseEntity<ApiResponseDto<BreachActivityResponseDto>> getEmailBreachActivity(
            @RequestParam(defaultValue = "30") int days
    );

    @Operation(summary = "Get breach dashboard summary counts")
    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<InsecureWebFindingsSummaryDto>> getFindingsSummary();

    @Operation(
            summary = "Live search of upstream InsecureWeb dark-web breaches",
            description = "Forwards the search to InsecureWeb using the operator-suffix query syntax "
                    + "(`breachStatus.in`, `domain.contains`, `email.contains`). Results are returned to "
                    + "the caller without persisting (use the regular sync flow for storage)."
    )
    @PostMapping("/search")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<InsecureWebBreachFindingDto>>>> searchExternalBreaches(
            @RequestBody @Valid InsecureWebBreachSearchRequest request
    );

    @Operation(summary = "List unique breach sources (paginated)")
    @GetMapping("/sources")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<String>>>> getSources(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "source") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    );
}
