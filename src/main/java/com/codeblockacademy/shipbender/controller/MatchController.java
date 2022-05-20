package com.codeblockacademy.shipbender.controller;

import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.dto.response.GameMatchDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    IMatchService matchService;

    public MatchController ( IMatchService matchService ) {
        this.matchService = matchService;
    }

    @GetMapping("/{game_id}/status")
    public StatusGameDTO statusGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.statusGame(authentication, game_id);
    }

    @GetMapping("/{game_id}")
    public GameMatchDTO dataGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.getGame(authentication, game_id);
    }

    // crear juegos
    @PostMapping(path = "/games/{nation}/{location}")
    public ResponseEntity<GameCreatedDTO> createGame ( Authentication authentication, @PathVariable NationType nation, @PathVariable String location ) {
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(matchService.createGame(authentication, nation, location));
    }

    // unirse a juegos
    @PostMapping(path = "/games/{id}")
    public GameCreatedDTO joinGame ( Authentication authentication, @PathVariable Long id ) {
        return matchService.joinGame(authentication, id);
    }
}
