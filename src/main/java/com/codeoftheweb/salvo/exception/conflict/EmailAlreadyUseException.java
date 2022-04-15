package com.codeoftheweb.salvo.exception.conflict;

public class EmailAlreadyUseException extends ConflictException {

    public EmailAlreadyUseException ( String email ) {
        super("The email <" + email + "> is already in use", "Email Is Already In Use Exception");
    }
}
