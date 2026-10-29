package com.authservice.authservice.service.passkey;

import com.authservice.authservice.dto.LoginResponse;
import com.authservice.authservice.dto.PasskeyOptionsResponse;

public interface PasskeyService {

    PasskeyOptionsResponse beginRegistration(Long userId);

    void finishRegistration(Long userId, String requestId, String registrationResponseJson);

    PasskeyOptionsResponse beginAuthentication(String username);

    LoginResponse finishAuthentication(String username, String requestId, String authenticationResponseJson);
}
