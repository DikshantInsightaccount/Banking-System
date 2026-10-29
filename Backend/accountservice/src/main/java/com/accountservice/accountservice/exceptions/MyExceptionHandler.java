package com.accountservice.accountservice.exceptions;

import com.accountservice.accountservice.utils.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleAccountNotFound(AccountNotFoundException e) {
        return ResponseEntity
                .status(404)
                .body(new ResponseMessage(e.getMessage()));
    }

    @ExceptionHandler(DepositNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleDepositNotFound(DepositNotFoundException e) {
        return ResponseEntity
                .status(404)
                .body(new ResponseMessage(e.getMessage()));
    }

    @ExceptionHandler(AccountResourceException.class)
    public ResponseEntity<ResponseMessage> handle(AccountResourceException e) {
        return ResponseEntity
                .status(400)
                .body(new ResponseMessage(e.getMessage()));
    }
}