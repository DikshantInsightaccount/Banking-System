package com.authservice.authservice.service;

import com.authservice.authservice.dto.LoginRequest;
import com.authservice.authservice.dto.LoginResponse;
import com.authservice.authservice.dto.RegisterRequest;
import com.authservice.authservice.entity.User;

public interface AuthService {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    User findByUsername(String username);

    boolean verifyPassword(Long userid, String rawPassword);
}