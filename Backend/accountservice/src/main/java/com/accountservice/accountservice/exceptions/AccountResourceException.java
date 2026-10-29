
package com.accountservice.accountservice.exceptions;

public class AccountResourceException extends RuntimeException {

    public AccountResourceException() {
    }

    public AccountResourceException(String message) {
        super(message);
    }
}