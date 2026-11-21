package dev.racnar.shipbender.shared.error.conflict;

public class GameIsFullException extends ConflictException {
    public GameIsFullException ( Long id ) {
        super("The game with id <" + id + "> is full", "Game Is Full");
    }
}
