package dev.racnar.shipbender.exception.forbidden;

import dev.racnar.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AirbenderException {
    public ForbiddenException ( String message, String exception ) {
        super(message, exception, HttpStatus.FORBIDDEN);
    }
}
