package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.news.NewsDto;
import com.aspire.asat.cms.dto.news.NewsReactionRequestDto;
import com.aspire.asat.cms.dto.news.UserReaction;
import com.aspire.asat.cms.dto.survey.SurveyDto;
import com.aspire.asat.cms.service.NewsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {

    private final WebClient.Builder webClientBuilder;
    private final String registrationServiceUrl;

    public NewsServiceImpl(WebClient.Builder webClientBuilder,
                           @Value("${service.registration.url}") String registrationServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.registrationServiceUrl = registrationServiceUrl;
    }

    public List<NewsDto> getNewsData(String userId) {
        // Use profile-specific URL from config (application-prod.yml / application-dev.yml / application-local.yml)
        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Call the API to get the news list, passing the userId as a query parameter
        List<NewsDto> newsResponseDtos = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/news")  // Replace with your actual API endpoint
                        .queryParam("userId", userId)  // Add the userId as a query parameter
                        .build())
                .retrieve()
                .bodyToFlux(NewsDto.class)  // Assuming the response body is a list of NewsDto objects
                .collectList()  // Collects all NewsDto items into a List
                .block();
        return newsResponseDtos;
    }

    public UserReaction handleUserReaction(String userId, String newsId) {

        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Send GET request to register user reaction with query parameters
        Mono<UserReaction> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/news/reaction")
                        .queryParam("userId", userId)  // Add userId as query parameter
                        .queryParam("newsId", newsId)  // Add newsId as query parameter
                        .build())  // Build the final URI
                .retrieve()
                .bodyToMono(UserReaction.class);  // Map the response directly to ApiResponse

        // Block to get the response synchronously and return it
        UserReaction apiResponse = response.block();  // Blocks the response until it's ready

        return apiResponse;
    }

    public String sendUserReaction(NewsReactionRequestDto newsReactionRequestDto) {

        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        // Send POST request to the /registration/api/v1/news/react endpoint with NewsReactionRequestDto as the body
        Mono<String> response = webClient.post()
                .uri("/news/react")
                .bodyValue(newsReactionRequestDto)  // Set the body with NewsReactionRequestDto
                .retrieve()
                .bodyToMono(String.class)  // Expecting a string response
                .onErrorReturn("Error occurred");  // Error handling

        // Block to get the response synchronously and return it
        String apiResponse = response.block();  // Blocks the response until it's ready

        return apiResponse != null ? apiResponse : "Unknown error";  // Return the message or a default message in case of an error
    }


}
