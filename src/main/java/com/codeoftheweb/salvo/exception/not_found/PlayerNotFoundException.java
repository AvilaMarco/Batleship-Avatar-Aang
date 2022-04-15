package com.codeoftheweb.salvo.exception.not_found;

public class PlayerNotFoundException extends NotFoundException {

    public PlayerNotFoundException ( String email ) {
        super("Player with email <" + email + "> not found", "Player Not Found Exception");
    }
}
