package com.codeoftheweb.salvo.utils;

public interface ENV_VARIABLES {
    String HEADER                   = "Authorization";
    String PREFIX                   = "Bearer ";
    String SECRET_KEY               = "mySecretKey";
    String CLAIMS                   = "AUTHORITIES";
    Long   MINUTES_TOKEN_EXPIRATION = 10L;
}
