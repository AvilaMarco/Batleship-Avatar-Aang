package com.codeoftheweb.salvo.exception.not_found;

import org.springframework.http.HttpStatus;

public class GameNotFoundException extends NotFoundException {

    public GameNotFoundException(Long id) {
        super("Game with id <" + id + "> not found", "Game Not Found Exception");
    }
}
