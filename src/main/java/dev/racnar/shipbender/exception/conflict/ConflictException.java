package dev.racnar.shipbender.exception.conflict;

import dev.racnar.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ConflictException extends AirbenderException {
    public ConflictException ( String message, String exception ) {
        super(message, exception, HttpStatus.CONFLICT);
    }
}
