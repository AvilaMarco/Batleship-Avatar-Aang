package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.player.dto.TokenResponse;
import dev.racnar.shipbender.security.JwtService;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtService       jwtService;

    public AuthService ( PlayerRepository playerRepository, PasswordEncoder passwordEncoder, JwtService jwtService ) {
        this.playerRepository = playerRepository;
        this.passwordEncoder  = passwordEncoder;
        this.jwtService       = jwtService;
    }

    public TokenResponse login ( String email, String password ) {
        Player player = playerRepository.findByEmail(email)
          .filter(p -> passwordEncoder.matches(password, p.getPassword()))
          .orElseThrow(() -> new ShipbenderException(ErrorCode.INVALID_CREDENTIALS, "Email o contrasena incorrectos"));
        return new TokenResponse(email, jwtService.getJWTToken(email, player.getRolls()));
    }
}
