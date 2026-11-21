package dev.racnar.shipbender.exception.not_found;

import dev.racnar.shipbender.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AirbenderException {

    public NotFoundException ( String message, String exception ) {
        super(message, exception, HttpStatus.NOT_FOUND);
    }
}
