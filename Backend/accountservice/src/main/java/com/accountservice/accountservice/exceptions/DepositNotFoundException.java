

package com.accountservice.accountservice.exceptions;

public class DepositNotFoundException extends AccountResourceException {

    public DepositNotFoundException() {
    }

    public DepositNotFoundException(String message) {
        super(message);
    }
}