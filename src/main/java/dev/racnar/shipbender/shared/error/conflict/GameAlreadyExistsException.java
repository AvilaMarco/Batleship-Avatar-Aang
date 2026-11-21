package dev.racnar.shipbender.shared.error.conflict;

public class GameAlreadyExistsException extends ConflictException {
    public GameAlreadyExistsException ( String message ) {
        super("There is a game on the location <" + message + ">", "Game is Already Exists");
    }
}
