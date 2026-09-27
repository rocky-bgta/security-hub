package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.leaderboard.LeaderboardDTO;
import com.aspire.asat.cms.dto.news.NewsReactionRequestDto;
import com.aspire.asat.cms.dto.survey.SurveyDto;
import com.aspire.asat.cms.dto.survey.SurveyUserDto;
import com.aspire.asat.cms.service.SurveyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class SurveyServiceImpl implements SurveyService {

    private final WebClient.Builder webClientBuilder;
    private final String registrationServiceUrl;

    public SurveyServiceImpl(WebClient.Builder webClientBuilder,
                             @Value("${service.registration.url}") String registrationServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.registrationServiceUrl = registrationServiceUrl;
    }

    public List<SurveyDto> getSurveyData(String userId) {
        // Use profile-specific URL from config (application-prod.yml / application-dev.yml / application-local.yml)
        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Make the GET request to the survey API with the userId as a query parameter
        Mono<SurveyDto[]> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/survey") // Endpoint
                        .queryParam("userId", userId) // Add userId as a query parameter
                        .build())
                .retrieve()
                .bodyToMono(SurveyDto[].class);  // Map the response directly to an array of SurveyDto

        // Block to get the response synchronously and return it as a List
        SurveyDto[] surveyData = response.block();  // Blocks the response until it's ready

        // Return the List from the array of SurveyDto objects
        return surveyData != null ? Arrays.asList(surveyData) : List.of();
    }

    public String sendUserReaction(SurveyUserDto surveyUserDto) {

        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Send POST request to the survey endpoint
        Mono<String> response = webClient.post()
                .uri("/survey")
                .bodyValue(surveyUserDto)  // Set the body with NewsReactionRequestDto
                .retrieve()
                .bodyToMono(String.class)  // Expecting a string response
                .onErrorReturn("Error occurred");  // Error handling

        // Block to get the response synchronously and return it
        String apiResponse = response.block();  // Blocks the response until it's ready

        return apiResponse != null ? apiResponse : "Unknown error";  // Return the message or a default message in case of an error
    }


}
