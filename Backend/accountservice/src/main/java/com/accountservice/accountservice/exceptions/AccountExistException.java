

package com.accountservice.accountservice.exceptions;

public class AccountExistException extends AccountResourceException {

    public AccountExistException() {
    }

    public AccountExistException(String message) {
        super(message);
    }
}