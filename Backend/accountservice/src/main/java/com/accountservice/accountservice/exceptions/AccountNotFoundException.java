
package com.accountservice.accountservice.exceptions;

public class AccountNotFoundException extends AccountResourceException {

    public AccountNotFoundException() {
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}