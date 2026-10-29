package com.transactionservice.transactionservice;

import com.transactionservice.transactionservice.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntime_returnsBadRequestResponse() {

        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, String>> response =
                handler.handleRuntime(ex);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Something went wrong",
                response.getBody().get("error"));
    }
}
