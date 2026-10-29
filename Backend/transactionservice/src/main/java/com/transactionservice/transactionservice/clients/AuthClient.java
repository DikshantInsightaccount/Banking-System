package com.transactionservice.transactionservice.clients;

import com.transactionservice.transactionservice.config.AuthFeignConfig;
import com.transactionservice.transactionservice.dto.VerifyPasswordRequest;
import com.transactionservice.transactionservice.dto.VerifyPasswordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "authservice",
        url = "${services.auth-service.url}",
        configuration = AuthFeignConfig.class
)
public interface AuthClient {

    @PostMapping("/auth/verify-password")
    VerifyPasswordResponse verifyPassword(
            @RequestBody VerifyPasswordRequest request
    );
}