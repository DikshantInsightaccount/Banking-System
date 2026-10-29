package com.accountservice.accountservice.clients;

import com.accountservice.accountservice.config.AuthFeignConfig;
import com.accountservice.accountservice.dto.VerifyPasswordRequest;
import com.accountservice.accountservice.dto.VerifyPasswordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "authservice",
        url = "${auth.service.base-url}",
        configuration = AuthFeignConfig.class
)
public interface AuthFeignClient {

    @PostMapping("/auth/verify-password")
    VerifyPasswordResponse verifyPassword(
            @RequestBody VerifyPasswordRequest request
    );
}