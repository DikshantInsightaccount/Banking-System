package com.authservice.authservice;

import com.authservice.authservice.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {


    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ✅ AuthenticationFailedException
    @Test
    void testHandleAlreadyExists() {
        ResourceAlreadyExistsException ex =
                new ResourceAlreadyExistsException("Resource exists");

        ResponseEntity<Map<String, String>> response =
                handler.handleAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Resource exists", response.getBody().get("error"));
    }

    @Test
    void testHandleAuthFailed() {
        AuthenticationFailedException ex =
                new AuthenticationFailedException("Invalid");

        ResponseEntity<Map<String, String>> response =
                handler.handleAuthFailed(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid", response.getBody().get("error"));
    }

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Not found");

        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntime() {
        RuntimeException ex = new RuntimeException("runtime");

        ResponseEntity<Map<String, String>> response =
                handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("runtime", response.getBody().get("error"));
    }
}
