package dev.racnar.shipbender.shared.error.not_found;

import dev.racnar.shipbender.shared.error.AirbenderException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AirbenderException {

    public NotFoundException ( String message, String exception ) {
        super(message, exception, HttpStatus.NOT_FOUND);
    }
}
