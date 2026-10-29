package com.authservice.authservice.controllers;

import com.authservice.authservice.dto.*;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.repository.UserRepository;
import com.authservice.authservice.service.passkey.PasskeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/passkeys")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;
    private final UserRepository userRepository;

    // BEGIN REGISTRATION
    @PostMapping("/registration/options")
    public ResponseEntity<PasskeyOptionsResponse> beginRegistration(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getUserid();

        return ResponseEntity.ok(
                passkeyService.beginRegistration(userId)
        );
    }

    // FINISH REGISTRATION
    @PostMapping("/registration/verify")
    public ResponseEntity<?> finishRegistration(
            Authentication authentication,
            @RequestBody PasskeyVerifyRequest req) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getUserid();

        passkeyService.finishRegistration(
                userId,
                req.getRequestId(),
                req.getCredentialJson()
        );

        return ResponseEntity.ok("Passkey registered");
    }

//    // BEGIN LOGIN
//    @PostMapping("/authentication/options")
//    public ResponseEntity<PasskeyOptionsResponse> beginAuthentication(
//            @RequestBody PasskeyBeginAuthRequest req) {
//
//        return ResponseEntity.ok(
//                passkeyService.beginAuthentication(req.getUsername())
//        );
//    }

    @PostMapping("/authentication/options")
    public ResponseEntity<PasskeyOptionsResponse> beginAuthentication(
            @RequestBody PasskeyBeginAuthRequest req) {

        System.out.println("🔥 Received username: " + req.getUsername());

        return ResponseEntity.ok(
                passkeyService.beginAuthentication(req.getUsername())
        );
    }

    // FINISH LOGIN
    @PostMapping("/authentication/verify")
    public ResponseEntity<LoginResponse> finishAuthentication(
            @RequestBody PasskeyVerifyAuthRequest req) {

        return ResponseEntity.ok(
                passkeyService.finishAuthentication(
                        req.getUsername(),
                        req.getRequestId(),
                        req.getCredentialJson()
                )
        );
    }
}