package com.authservice.authservice.controllers;

import com.authservice.authservice.dto.*;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.service.AuthService;
import com.authservice.authservice.service.OtpService;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    // SEND OTP TO EMAIL (query param)
    // POST /auth/send-otp?email=someone@gmail.com
    @PostMapping("/send-otp")
    public ResponseEntity<GenericMessageResponse> sendOtp(@RequestParam String email) {

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new GenericMessageResponse("Email is required"));
        }

        return ResponseEntity.ok(otpService.sendOtp(email.trim()));
    }

    // VERIFY OTP FROM EMAIL (JSON body)
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyPasswordResponse> verifyOtp(@RequestBody VerifyOtpEmailRequest req) {
        boolean ok = otpService.verifyOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.ok(new VerifyPasswordResponse(ok));
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // VERIFY PASSWORD
    @PostMapping("/verify-password")
    public VerifyPasswordResponse verifyPassword(
            @RequestBody VerifyPasswordRequest req) {

        System.out.println("=================================");
        System.out.println("AUTH VERIFY PASSWORD HIT");
        System.out.println("USER ID = " + req.getUserId());
        System.out.println("PASSWORD RECEIVED");
        System.out.println("=================================");

        boolean ok =
                authService.verifyPassword(
                        req.getUserId(),
                        req.getPassword()
                );

        System.out.println("PASSWORD VALID = " + ok);

        return new VerifyPasswordResponse(ok);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // TEST (JWT required)
    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }
}