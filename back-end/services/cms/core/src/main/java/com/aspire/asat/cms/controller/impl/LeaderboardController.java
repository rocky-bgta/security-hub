package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.leaderboard.LeaderboardDTO;
import com.aspire.asat.cms.service.LeaderboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping(value ="/api/v1/leaderboard", produces = "application/json")
public class LeaderboardController {

    private LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<LeaderboardDTO>>> getMappedLeaderboard() {
        // Call the service method to get data and return it as a reactive Flux
List<LeaderboardDTO> leaderboardData = leaderboardService.getLeaderboardData();
        ApiResponseDto<List<LeaderboardDTO>> response = new ApiResponseDto<>("Leaderboard retrieved successfully", HttpStatus.OK.value(),leaderboardData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
