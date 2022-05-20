package com.codeblockacademy.shipbender.exception.not_found;

public class LoginException extends NotFoundException {
    public LoginException () {
        super("Please check the email or password", "Login Exception");
    }
}
