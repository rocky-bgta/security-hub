package com.aspire.asat.billing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponseDTO<T> {
    private boolean error;
    private String message;
    private int statusCode;
    private T data;
    private long total;
    private String path;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public static <T> ResponseEntity<CommonResponseDTO<T>> success(T data, String message) {
        return success(data, message, data instanceof Collection<?> ? ((Collection<?>) data).size() : 0);
    }

    public static <T> ResponseEntity<CommonResponseDTO<T>> success(T data, String message, long total) {
        CommonResponseDTO<T> response = CommonResponseDTO.<T>builder()
                .error(false)
                .message(message)
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .total(total)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<CommonResponseDTO<T>> success(T data, String message, long total, HttpStatus status) {
        CommonResponseDTO<T> response = CommonResponseDTO.<T>builder()
                .error(false)
                .message(message)
                .statusCode(status.value())
                .data(data)
                .total(total)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}

