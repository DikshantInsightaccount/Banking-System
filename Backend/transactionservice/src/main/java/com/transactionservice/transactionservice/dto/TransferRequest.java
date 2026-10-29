package com.transactionservice.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class TransferRequest {

    private Long fromAccountId;
    private Long toAccountId;

    private String fromAccountNumber;
    private String toAccountNumber;

//    @JsonProperty("userid")
    private Long userId;

    private String password;
    private BigDecimal amount;

    public TransferRequest() {}

    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    public void setIdempotencyKey(
            String idempotencyKey
    ) {
        this.idempotencyKey =
                idempotencyKey;
    }
}

