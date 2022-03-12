package com.codeoftheweb.salvo.exception.forbidden;

import com.codeoftheweb.salvo.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AirbenderException {
    public ForbiddenException(String message, String exception) {
        super(message, exception, HttpStatus.FORBIDDEN);
    }
}
