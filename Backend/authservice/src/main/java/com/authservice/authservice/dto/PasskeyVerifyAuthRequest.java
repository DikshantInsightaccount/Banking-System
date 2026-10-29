package com.authservice.authservice.dto;

import lombok.Data;

@Data
public class PasskeyVerifyAuthRequest {
    private String requestId;
    private String username;
    private String credentialJson; // JSON string from browser: assertion.toJSON()
}