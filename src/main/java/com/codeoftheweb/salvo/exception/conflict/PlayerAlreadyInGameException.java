package com.codeoftheweb.salvo.exception.conflict;

public class PlayerAlreadyInGameException extends ConflictException {
    public PlayerAlreadyInGameException ( Long id ) {
        super("You already in the game " + id, "Player Already in Game");
    }
}
