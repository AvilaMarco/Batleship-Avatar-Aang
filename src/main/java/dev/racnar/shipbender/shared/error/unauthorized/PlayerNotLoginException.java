package dev.racnar.shipbender.shared.error.unauthorized;

public class PlayerNotLoginException extends UnauthorizedException {

    public PlayerNotLoginException () {
        super("Player session not found, you need login", "Player Not Login Exception");
    }
}
