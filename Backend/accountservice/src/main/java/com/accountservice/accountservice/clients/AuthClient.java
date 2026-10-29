package com.accountservice.accountservice.clients;

import com.accountservice.accountservice.dto.VerifyPasswordRequest;
import com.accountservice.accountservice.dto.VerifyPasswordResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {

    private final RestTemplate restTemplate;

    // Read base URL from application.yml
    @Value("${auth.service.base-url}")
    private String baseUrl;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void verifyOrThrow(Long userid, String password) {

        // Basic validation
        if (userid == null || password == null || password.isBlank()) {
            throw new RuntimeException("Password is required to proceed");
        }

        // Use service name (Eureka + LoadBalancer)
        String url = baseUrl + "/auth/verify-password";

        VerifyPasswordRequest req = new VerifyPasswordRequest();
        req.setUserId(userid);
        req.setPassword(password);

        ResponseEntity<VerifyPasswordResponse> res =
                restTemplate.postForEntity(
                        url,
                        req,
                        VerifyPasswordResponse.class
                );

        VerifyPasswordResponse body = res.getBody();

        // ✅ Validate response
        if (body == null || !body.isValid()) {
            throw new RuntimeException("Invalid password");
        }
    }
}