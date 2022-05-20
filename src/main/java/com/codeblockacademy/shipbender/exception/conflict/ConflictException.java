package com.codeblockacademy.shipbender.exception.conflict;

import com.codeblockacademy.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ConflictException extends AirbenderException {
    public ConflictException ( String message, String exception ) {
        super(message, exception, HttpStatus.CONFLICT);
    }
}
