package com.transactionservice.transactionservice.exceptions;

public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(String message) {
        super(message);
    }
}