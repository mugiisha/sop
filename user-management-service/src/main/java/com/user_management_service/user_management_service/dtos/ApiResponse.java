package com.user_management_service.user_management_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    @Schema(description = "Response status code")
    public int getStatus() {
        return status;
    }

    @Schema(description = "Response message")
    public String getMessage() {
        return message;
    }

    @Schema(description = "Response data")
    public T getData() {
        return data;
    }
}