package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.dto.UserTokenDTO;
import dev.racnar.shipbender.shared.error.not_found.LoginException;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyStoreException;

@Service
public class SessionService {

    PlayerRepository playerRepository;
    PasswordEncoder  passwordEncoder;
    JWTUtil          jwtUtil;

    public SessionService ( PlayerRepository playerRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil ) {
        this.playerRepository = playerRepository;
        this.passwordEncoder  = passwordEncoder;
        this.jwtUtil          = jwtUtil;
    }

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
