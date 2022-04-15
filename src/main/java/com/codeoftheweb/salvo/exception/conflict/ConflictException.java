package com.codeoftheweb.salvo.exception.conflict;

import com.codeoftheweb.salvo.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ConflictException extends AirbenderException {
    public ConflictException ( String message, String exception ) {
        super(message, exception, HttpStatus.CONFLICT);
    }
}
