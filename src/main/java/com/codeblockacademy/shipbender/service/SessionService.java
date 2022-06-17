package com.codeblockacademy.shipbender.service;

import com.codeblockacademy.shipbender.dto.response.UserTokenDTO;
import com.codeblockacademy.shipbender.exception.not_found.LoginException;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.repository.PlayerRepository;
import com.codeblockacademy.shipbender.security.JWTUtil;
import com.codeblockacademy.shipbender.service.intereface.ISessionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyStoreException;

@Service
public class SessionService implements ISessionService {

    PlayerRepository playerRepository;
    PasswordEncoder  passwordEncoder;
    JWTUtil          jwtUtil;

    public SessionService ( PlayerRepository playerRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil ) {
        this.playerRepository = playerRepository;
        this.passwordEncoder  = passwordEncoder;
        this.jwtUtil          = jwtUtil;
    }

    @Override
    public UserTokenDTO login ( String username, String password ) throws KeyStoreException {
        //Voy a la base de datos y reviso que el usuario y contraseña existan.
        Player player = playerRepository
          .findByEmail(username)
          .orElseThrow(LoginException::new);

        if (!passwordEncoder.matches(password, player.getPassword())) {
            throw new LoginException();
        }

        String token = jwtUtil.getJWTToken(username, player.getRolls());
        return new UserTokenDTO(username, token);
    }

}
