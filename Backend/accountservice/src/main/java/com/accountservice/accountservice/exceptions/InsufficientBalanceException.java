package com.accountservice.accountservice.exceptions;

public class InsufficientBalanceException extends AccountResourceException {

    public InsufficientBalanceException() {
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}