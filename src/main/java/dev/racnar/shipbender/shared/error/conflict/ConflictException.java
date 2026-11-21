package dev.racnar.shipbender.shared.error.conflict;

import dev.racnar.shipbender.shared.error.AirbenderException;
import org.springframework.http.HttpStatus;

public class ConflictException extends AirbenderException {
    public ConflictException ( String message, String exception ) {
        super(message, exception, HttpStatus.CONFLICT);
    }
}
