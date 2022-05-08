package com.codeoftheweb.salvo.exception.handler;

import com.codeoftheweb.salvo.dto.error.ErrorDTO;
import com.codeoftheweb.salvo.exception.AirbenderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(AirbenderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ErrorDTO> playerNotFound ( AirbenderException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError(exception.getException());
        errorDTO.setMessage(exception.getMessage());
        return ResponseEntity
          .status(exception.getStatus())
          .body(errorDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorDTO> enumError ( IllegalArgumentException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Conflict Name Exception");
        errorDTO.setMessage(exception.getMessage());
        return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(errorDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorDTO> validatingDataTypes ( MethodArgumentNotValidException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Payload's Field Not Valid Exception");
        errorDTO.setMessage("There are some fields that don't respect validations");

        HashMap<String, List<String>> errors = new HashMap<>();

        exception.getFieldErrors()
          .forEach(e -> {
              String field = e.getField();
              String msg   = e.getDefaultMessage();

              errors.compute(field, ( $, l ) ->
                new ArrayList<>() {
                    {
                        addAll(!Objects.isNull(l) ? l : new ArrayList<>());
                        add(msg);
                    }
                }
              );
          });
        errorDTO.setErrorFields(errors);
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    ResponseEntity<ErrorDTO> loginFail ( UsernameNotFoundException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Email Not Found");
        errorDTO.setMessage(exception.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorDTO> loginFail2 ( AuthenticationException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Fail Login");
        errorDTO.setMessage(exception.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    ResponseEntity<ErrorDTO> loginFail23 ( InsufficientAuthenticationException exception ) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Fail Login");
        errorDTO.setMessage(exception.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
