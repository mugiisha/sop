package com.user_management_service.user_management_service.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericResponse<T> {
    private int status;
    private String message;
    private T data;

    public GenericResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
