package com.authservice.authservice.dto;

public class VerifyPasswordRequest {
    private Long userId;
    private String password;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
