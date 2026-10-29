package com.accountservice.accountservice.dto;

import com.accountservice.accountservice.entity.Status;

public class StatusRequest {
    private Status status;

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}