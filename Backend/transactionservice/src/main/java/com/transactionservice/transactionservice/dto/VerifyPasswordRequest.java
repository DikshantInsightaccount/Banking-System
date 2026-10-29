package com.transactionservice.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyPasswordRequest {
    @JsonProperty("userId")
    private Long userId;
    private String password;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}