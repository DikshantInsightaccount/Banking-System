package com.authservice.authservice.dto;

public class GenericMessageResponse {
    private String message;

    public GenericMessageResponse() {}
    public GenericMessageResponse(String message) { this.message = message; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}