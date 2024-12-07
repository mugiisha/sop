package com.sop_workflow_service.sop_workflow_service.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String message;
    private T data;
}