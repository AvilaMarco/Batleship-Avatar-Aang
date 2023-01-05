package com.codeblockacademy.shipbender.exception.unauthorized;

import com.codeblockacademy.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AirbenderException {

    public UnauthorizedException ( String message, String exception ) {
        super(message, exception, HttpStatus.UNAUTHORIZED);
    }
}
