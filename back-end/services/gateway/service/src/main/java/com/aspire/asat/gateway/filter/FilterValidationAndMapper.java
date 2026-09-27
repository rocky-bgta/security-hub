package com.aspire.asat.gateway.filter;

import com.aspire.asat.gateway.dto.enums.ErrorMessages;
import com.aspire.asat.gateway.util.CustomDataConfiguration;
import com.aspire.asat.gateway.util.JacksonUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FilterValidationAndMapper {
    private FilterValidationAndMapper() {
    }

    public static Mono<Void> onError(ServerWebExchange exchange, ErrorMessages errorMessages) {
        final ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(errorMessages.getHttpStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] errorBody = prepareJsonPayload(errorMessages.getMessage(), errorMessages.getCode()).getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Flux.just(new DefaultDataBufferFactory().wrap(errorBody)));
    }

    @SneakyThrows
    public static String prepareJsonPayload(String responseMessage, String responseCode) {
        Map<String, String> map = new HashMap<>();
        map.put("responseCode", responseCode);
        map.put("responseMessage", responseMessage);
        return JacksonUtil.objectToJson(map);
    }

    public static String bearerAccessToken(ServerHttpRequest request) {
        var token = request.getHeaders().getFirst(CustomDataConfiguration.HEADER_AUTHORIZATION);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return token.trim();
    }

    public static boolean isArrayLengthOk(String[] arr, int length) {
        return arr.length == length;
    }

    public static boolean isArrayLengthNotOk(String[] arr, int length) {
        return !isArrayLengthOk(arr, length);
    }

    public static boolean isTokenPrefixOK(String tokenPrefix, String[] arr) {
        return tokenPrefix.equals(arr[0]);
    }

    public static boolean isTokenPrefixNotOK(String tokenPrefix, String[] arr) {
        return !isTokenPrefixOK(tokenPrefix, arr);
    }

}
