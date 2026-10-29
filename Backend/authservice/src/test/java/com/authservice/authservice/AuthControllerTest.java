package com.authservice.authservice;

import com.authservice.authservice.dto.*;
import com.authservice.authservice.entity.Role;
import com.authservice.authservice.service.AuthService;
import com.authservice.authservice.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ REGISTER – SUCCESS
    @Test
    void register_success() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        User savedUser = new User();
        savedUser.setUserid(1L);
        savedUser.setUsername("john");
        savedUser.setRole(Role.CUSTOMER);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(savedUser);

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ✅ LOGIN – SUCCESS
    @Test
    void login_success() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse();
        response.setToken("jwt-token-123");
        response.setUserid(1L);
        response.setUsername("john");
        response.setRole("CUSTOMER");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    // ✅ VERIFY PASSWORD – VALID
    @Test
    void verifyPassword_valid() throws Exception {

        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setUserId(1L);
        request.setPassword("password123");

        when(authService.verifyPassword(1L, "password123"))
                .thenReturn(true);

        mvc.perform(post("/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    // ✅ VERIFY PASSWORD – INVALID
    @Test
    void verifyPassword_invalid() throws Exception {

        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setUserId(1L);
        request.setPassword("wrong");

        when(authService.verifyPassword(1L, "wrong"))
                .thenReturn(false);

        mvc.perform(post("/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
