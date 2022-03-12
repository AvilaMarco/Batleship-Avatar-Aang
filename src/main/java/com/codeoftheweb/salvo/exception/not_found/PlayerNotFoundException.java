package com.codeoftheweb.salvo.exception.not_found;

import lombok.Getter;
import org.springframework.http.HttpStatus;


public class PlayerNotFoundException extends  NotFoundException {

    public PlayerNotFoundException(String email) {
        super("Player with email <" + email + "> not found", "Player Not Found Exception");
    }
}
