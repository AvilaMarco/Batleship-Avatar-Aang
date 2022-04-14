package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.enums.NationType;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.intereface.ISalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    ISalvoService salvoService;

    @Autowired
    private com.codeoftheweb.salvo.repository.PlayerRepository PlayerRepository;

    @PostMapping(path = "")
    public PlayerDTO register(@Valid @RequestBody SignInPlayerDTO playerDTO) {
        return salvoService.registerPlayer(playerDTO);
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    //setear nacion player
    @RequestMapping(path = "/nation/{nacion}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setnacion(Authentication authentication, @PathVariable String nacion) {
        Map<String, Object> respuesta = new HashMap<>();
        if (!isGuest(authentication)) {
            Player player = PlayerRepository.findByEmail(authentication.getName()).get();
            if (player != null) {
                player.setNation(NationType.valueOf(nacion));
                PlayerRepository.save(player);
                respuesta.put("good", "nice");
                return new ResponseEntity<>(respuesta, HttpStatus.ACCEPTED);
            } else {
                respuesta.put("error", "you need to login");
                return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
            }
        } else {
            respuesta.put("error", "you need to login");
            return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
        }
    }
}
