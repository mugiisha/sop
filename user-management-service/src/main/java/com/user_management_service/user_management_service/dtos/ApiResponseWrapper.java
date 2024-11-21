package com.user_management_service.user_management_service.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseWrapper<T> {
    private int status;
    private String message;
    private T data;
}