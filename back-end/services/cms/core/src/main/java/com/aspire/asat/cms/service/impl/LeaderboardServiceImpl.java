package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.leaderboard.LeaderboardDTO;
import com.aspire.asat.cms.dto.leaderboard.LeaderboardResponse;
import com.aspire.asat.cms.service.LeaderboardService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final WebClient.Builder webClientBuilder;
    private final String registrationServiceUrl;

    public LeaderboardServiceImpl(WebClient.Builder webClientBuilder,
                                  @Value("${service.registration.url}") String registrationServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.registrationServiceUrl = registrationServiceUrl;
    }

    public List<LeaderboardDTO> getLeaderboardData() {
        // Use profile-specific URL from config (application-prod.yml / application-dev.yml / application-local.yml)
        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Make the GET request to the leaderboard API and map the response to an array of LeaderboardDTO
        Mono<LeaderboardDTO[]> response = webClient.get()
                .uri("/leaderboard")  // Ensure you are using the correct endpoint
                .retrieve()
                .bodyToMono(LeaderboardDTO[].class);  // Map the response directly to an array of LeaderboardDTO

        // Block to get the response synchronously and return it as a List
        LeaderboardDTO[] leaderboardData = response.block();  // Blocks the response until it's ready

        // Return the List from the array of LeaderboardDTO objects
        return leaderboardData != null ? Arrays.asList(leaderboardData) : List.of();
    }

}
