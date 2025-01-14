package com.analytics_insights_service.analytics_insights_service.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String message;
    private T data;
}