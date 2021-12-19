package com.codeoftheweb.salvo.exception.unauthorized;

import com.codeoftheweb.salvo.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AirbenderException {

    public UnauthorizedException(String message, String exception){
        super(message, exception, HttpStatus.UNAUTHORIZED);
    }
}
