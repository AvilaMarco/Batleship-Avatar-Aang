package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.service.intereface.ISalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    ISalvoService salvoService;

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
