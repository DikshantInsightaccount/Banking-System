package com.authservice.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class PasskeyOptionsResponse {
    private String requestId;       // server-side ceremony id
    private Map<String, Object> publicKey; // WebAuthn options
}