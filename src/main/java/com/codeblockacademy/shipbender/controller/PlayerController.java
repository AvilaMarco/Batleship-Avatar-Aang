package com.codeblockacademy.shipbender.controller;

import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.request.LoginPlayerDTO;
import com.codeblockacademy.shipbender.dto.request.SignInPlayerDTO;
import com.codeblockacademy.shipbender.dto.response.UserTokenDTO;
import com.codeblockacademy.shipbender.service.intereface.ISalvoService;
import com.codeblockacademy.shipbender.service.intereface.ISessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.KeyStoreException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    ISalvoService   salvoService;
    ISessionService sessionService;

    public PlayerController ( ISalvoService salvoService, ISessionService sessionService ) {
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
