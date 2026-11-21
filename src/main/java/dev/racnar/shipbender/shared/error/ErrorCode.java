package dev.racnar.shipbender.shared.error;

public enum ErrorCode {
    VALIDATION_FAILED(400),
    MALFORMED_REQUEST(400),
    INVALID_FLEET(400),
    INVALID_SALVO(400),
    UNAUTHENTICATED(401),
    INVALID_CREDENTIALS(401),
    FORBIDDEN(403),
    NOT_A_PARTICIPANT(403),
    GAME_NOT_FOUND(404),
    EMAIL_IN_USE(409),
    LOCATION_TAKEN(409),
    GAME_FULL(409),
    ALREADY_IN_GAME(409),
    SHIPS_ALREADY_PLACED(409),
    GAME_NOT_IN_PROGRESS(409),
    SALVO_ALREADY_FIRED(409),
    INTERNAL_ERROR(500);

    private final int status;

    ErrorCode ( int status ) {
        this.status = status;
    }

    public int status () {
        return status;
    }
}
