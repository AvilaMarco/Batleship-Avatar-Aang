package com.codeoftheweb.salvo.exception.not_found;

import com.codeoftheweb.salvo.exception.AirbenderException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AirbenderException {

    public NotFoundException(String message, String exception){
        super(message, exception, HttpStatus.NOT_FOUND);
    }
}
