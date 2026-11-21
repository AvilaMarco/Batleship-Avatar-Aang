package dev.racnar.shipbender.shared.error.unauthorized;

import dev.racnar.shipbender.shared.error.AirbenderException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AirbenderException {

    public UnauthorizedException ( String message, String exception ) {
        super(message, exception, HttpStatus.UNAUTHORIZED);
    }
}
