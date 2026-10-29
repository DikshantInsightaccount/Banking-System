package com.authservice.authservice;

import com.authservice.authservice.dto.LoginRequest;
import com.authservice.authservice.dto.LoginResponse;
import com.authservice.authservice.dto.RegisterRequest;
import com.authservice.authservice.entity.Role;
import com.authservice.authservice.entity.Status;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.exceptions.AuthenticationFailedException;
import com.authservice.authservice.exceptions.ResourceAlreadyExistsException;
import com.authservice.authservice.exceptions.ResourceNotFoundException;
import com.authservice.authservice.repository.UserRepository;
import com.authservice.authservice.security.JwtUtil;
import com.authservice.authservice.service.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================
    // REGISTER
    // =========================

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        User user = authService.register(request);

        assertEquals("john", user.getUsername());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    void register_userAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(request));
    }

    // =========================
    // LOGIN
    // =========================

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User user = new User();
        user.setUserid(1L);
        user.setUsername("john");
        user.setPasswordhash("hashed");
        user.setRole(Role.CUSTOMER);
        user.setStatus(Status.ACTIVE);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed"))
                .thenReturn(true);
        when(jwtUtil.generateToken("john", "CUSTOMER"))
                .thenReturn("jwt-token-123");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token-123", response.getToken());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrong");

        User user = new User();
        user.setPasswordhash("hashed");
        user.setStatus(Status.ACTIVE);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed"))
                .thenReturn(false);

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(request));
    }

    @Test
    void login_userInactive() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User user = new User();
        user.setPasswordhash("hashed");
        user.setStatus(Status.INACTIVE);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed"))
                .thenReturn(true);

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(request));
    }

    // =========================
    // VERIFY PASSWORD
    // =========================

    @Test
    void verifyPassword_true() {
        User user = new User();
        user.setPasswordhash("hashed");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed"))
                .thenReturn(true);

        assertTrue(authService.verifyPassword(1L, "password123"));
    }

    @Test
    void verifyPassword_userNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.verifyPassword(1L, "password123"));
    }
}