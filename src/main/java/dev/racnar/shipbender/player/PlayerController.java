package dev.racnar.shipbender.player;

import dev.racnar.shipbender.player.dto.PlayerDTO;
import dev.racnar.shipbender.player.dto.LoginPlayerDTO;
import dev.racnar.shipbender.player.dto.SignInPlayerDTO;
import dev.racnar.shipbender.player.dto.UserTokenDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.KeyStoreException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    SalvoService   salvoService;
    SessionService sessionService;

    public PlayerController ( SalvoService salvoService, SessionService sessionService ) {
        this.salvoService   = salvoService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserTokenDTO> login ( @Valid @RequestBody LoginPlayerDTO playerDTO ) throws KeyStoreException {
        return ResponseEntity.ok(sessionService.login(playerDTO.getEmail(), playerDTO.getPassword()));
    }

    @PostMapping(path = "")
    public ResponseEntity<PlayerDTO> register ( @Valid @RequestBody SignInPlayerDTO playerDTO ) {
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(salvoService.registerPlayer(playerDTO));
    }

    @PostMapping(path = "/nation/{nation}")
    public PlayerDTO setnacion ( Authentication authentication, @PathVariable String nation ) {
        return salvoService.setNationPlayer(authentication, nation);
    }
}
