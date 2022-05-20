package com.codeblockacademy.shipbender.exception.forbidden;

import com.codeblockacademy.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AirbenderException {
    public ForbiddenException ( String message, String exception ) {
        super(message, exception, HttpStatus.FORBIDDEN);
    }
}
