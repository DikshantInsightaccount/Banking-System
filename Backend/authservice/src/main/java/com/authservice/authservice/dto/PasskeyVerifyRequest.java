package com.authservice.authservice.dto;

import lombok.Data;

@Data
public class PasskeyVerifyRequest {
    private String requestId;
    private String credentialJson; // JSON string from browser: publicKeyCredential.toJSON()
}