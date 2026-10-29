package com.authservice.authservice.dto;

public class VerifyPasswordResponse {
    private boolean valid;

    public VerifyPasswordResponse() {}

    public VerifyPasswordResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}