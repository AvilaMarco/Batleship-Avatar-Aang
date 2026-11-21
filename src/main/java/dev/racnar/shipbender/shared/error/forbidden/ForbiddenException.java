package dev.racnar.shipbender.shared.error.forbidden;

import dev.racnar.shipbender.shared.error.AirbenderException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AirbenderException {
    public ForbiddenException ( String message, String exception ) {
        super(message, exception, HttpStatus.FORBIDDEN);
    }
}
