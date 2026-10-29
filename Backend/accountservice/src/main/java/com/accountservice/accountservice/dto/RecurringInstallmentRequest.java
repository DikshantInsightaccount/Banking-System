package com.accountservice.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class RecurringInstallmentRequest {

    private Long userId;
    private String password;

    /*
     * The due date the client intends to pay.
     * This remains identical when a failed HTTP request is retried.
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public RecurringInstallmentRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(
            LocalDate dueDate
    ) {
        this.dueDate = dueDate;
    }
}