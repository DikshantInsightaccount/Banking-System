package com.accountservice.accountservice.dto;

import com.accountservice.accountservice.entity.FixedDeposit;
import com.accountservice.accountservice.entity.RecurringDeposit;

public class SecureDepositRequest {

    private Long userId;
    private String password;

    private FixedDeposit fd;
    private RecurringDeposit rd;
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(
            String idempotencyKey
    ) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public FixedDeposit getFd() { return fd; }
    public void setFd(FixedDeposit fd) { this.fd = fd; }

    public RecurringDeposit getRd() { return rd; }
    public void setRd(RecurringDeposit rd) { this.rd = rd; }
}