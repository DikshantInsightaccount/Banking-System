package com.authservice.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "webauthn")
public class WebAuthnProperties {
    private String rpId;
    private String rpName;
    private String origin;
    private long timeoutMs = 60000;
    private long challengeTtlSeconds = 300;
}