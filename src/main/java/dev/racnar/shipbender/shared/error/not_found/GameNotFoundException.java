package dev.racnar.shipbender.shared.error.not_found;

public class GameNotFoundException extends NotFoundException {

    public GameNotFoundException ( Long id ) {
        super("Game with id <" + id + "> not found", "Game Not Found Exception");
    }
}
