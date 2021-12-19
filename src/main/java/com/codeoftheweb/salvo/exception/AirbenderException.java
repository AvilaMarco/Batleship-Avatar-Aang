package com.codeoftheweb.salvo.exception;

import org.springframework.http.HttpStatus;

public class AirbenderException extends RuntimeException {
    String message;
    String exception;
    HttpStatus status;

    public AirbenderException(String message, String exception, HttpStatus status){
        this.message    = message;
        this.exception  = exception;
        this.status     = status;
    }
}
