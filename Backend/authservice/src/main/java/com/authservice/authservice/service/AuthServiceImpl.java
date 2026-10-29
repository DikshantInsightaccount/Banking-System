package com.authservice.authservice.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with username: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordhash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setStatus(Status.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordhash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (user.getStatus() != Status.ACTIVE) {
            throw new AuthenticationFailedException("User is INACTIVE");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(token, user.getUserid(), user.getUsername(), user.getRole().name());
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // NEW METHOD: verify raw password against stored passwordhash
    @Override
    public boolean verifyPassword(
            Long userid,
            String rawPassword) {

        System.out.println(
                "VERIFY PASSWORD SERVICE START"
        );

        User user = userRepository.findById(userid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userid
                        )
                );

        System.out.println(
                "USER FOUND = " +
                        user.getUsername()
        );

        boolean valid =
                passwordEncoder.matches(
                        rawPassword,
                        user.getPasswordhash()
                );

        System.out.println(
                "PASSWORD MATCH RESULT = " +
                        valid
        );

        return valid;
    }
}