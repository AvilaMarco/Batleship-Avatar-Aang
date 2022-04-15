package com.codeoftheweb.salvo.exception.not_found;

public class GameNotFoundException extends NotFoundException {

    public GameNotFoundException ( Long id ) {
        super("Game with id <" + id + "> not found", "Game Not Found Exception");
    }
}
