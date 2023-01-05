package com.codeblockacademy.shipbender.config;

import com.auth0.jwt.algorithms.Algorithm;

public interface ENV_VARIABLES {
    String HEADER                   = "Authorization";
    String PREFIX                   = "Bearer ";
    String SECRET_KEY               = "mySecretKey";
    String CLAIMS                   = "AUTHORITIES";
    Long   MINUTES_TOKEN_EXPIRATION = 365241780471L;

    Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY.getBytes());
}
