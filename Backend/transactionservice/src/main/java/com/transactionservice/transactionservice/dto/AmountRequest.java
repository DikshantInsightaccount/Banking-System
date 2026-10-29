package com.transactionservice.transactionservice.dto;

import java.math.BigDecimal;

public class AmountRequest {

    private Long accountId;          // backward compatible
    private String accountNumber;    // NEW
    private BigDecimal amount;

    public AmountRequest() {
        //demonstrating amount request
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}