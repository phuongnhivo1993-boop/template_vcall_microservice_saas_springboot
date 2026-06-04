package com.vcall.webhooks.dto;

import lombok.Data;

@Data
public class TestResultResponse {
    private boolean success;
    private int statusCode;
    private String message;
}
