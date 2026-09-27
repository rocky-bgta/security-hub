package com.aspire.asat.universal.controller.supportTicket;

import com.aspire.asat.universal.constant.WebApiUrlConstants;
import com.aspire.asat.universal.supportTicket.response.SupportResolutionTimeResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Support Resolution Time APIs",
        description = "Endpoints for average support ticket resolution time and SLA compliance metrics")
@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT + "/support-resolution-time")
public interface SupportResolutionTimeController {

    @Operation(
            summary = "Get support resolution time metrics",
            description = "Returns overall average resolution time for CLOSED tickets (updatedDate - createdDate), "
                    + "SLA compliance (closed / total), and the same metrics broken down by support ticket type "
                    + "including open, closed, and in-progress counts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Support resolution time metrics fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(produces = "application/json")
    ResponseEntity<ApiResponseDto<SupportResolutionTimeResponseDto>> getSupportResolutionTime(
            @RequestParam(required = false) String clientAdminId
    );

    @Operation(
            summary = "Export support resolution time by type as CSV",
            description = "Downloads a CSV of support ticket type metrics: average resolution time, total tickets, "
                    + "open/closed/in-progress counts, and SLA compliance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV exported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/export", produces = "text/csv")
    void exportSupportResolutionTimeCsv(
            @RequestParam(required = false) String clientAdminId,
            HttpServletResponse response
    );
}
