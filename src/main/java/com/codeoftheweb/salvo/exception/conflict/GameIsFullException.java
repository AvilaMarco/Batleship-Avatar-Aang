package com.codeoftheweb.salvo.exception.conflict;

public class GameIsFullException extends ConflictException{
    public GameIsFullException(Long id) {
        super("The game with id <" + id + "> is full", "Game Is Full");
    }
}
