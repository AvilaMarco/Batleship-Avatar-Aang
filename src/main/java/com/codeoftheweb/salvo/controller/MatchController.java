package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.error.ErrorDTO;
import com.codeoftheweb.salvo.dto.request.websocket.EmoteDTO;
import com.codeoftheweb.salvo.dto.request.websocket.ShipDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.dto.response.StatusGameDTO;
import com.codeoftheweb.salvo.service.intereface.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    IMatchService matchService;

    @GetMapping("/{game_id}/status")
    public StatusGameDTO statusGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.statusGame(authentication, game_id);
    }

    @GetMapping("/{game_id}")
    public GameMatchDTO dataGame ( @PathVariable Long game_id, Authentication authentication ) {
        return matchService.getGame(authentication, game_id);
    }

    /* WEB SOCKETS */

    // enviar barcos
    @MessageMapping("/{gameId}/ships")
    @SendTo("/topic/match/{gameId}/ships")
    public ResponseEntity<StatusGameDTO> matchShips ( Authentication authentication, @DestinationVariable Long gameId, @RequestBody List<ShipDTO> ships ) {
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(matchService.createShips(authentication, gameId, ships));
    }


    // enviar emotes
    @MessageMapping("/{gameId}/emotes/{userId}") // entrada
    @SendTo("/topic/match/{gameId}/emotes/{userId}") // salida
    public ErrorDTO matchEmotes ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId, @RequestBody EmoteDTO emote ) {
        System.out.println(gameId);
        System.out.println(emote);
        return new ErrorDTO("emotes", "creating emotes" + userId);
    }

    // enviar disparos
    @MessageMapping("/{gameId}/salvos")
    @SendTo("/topic/match/{gameId}/salvos")
    public ResponseEntity<ErrorDTO> matchSalvos ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId ) {
        System.out.println(gameId);
        ErrorDTO a = new ErrorDTO("emotes", "creating emotes");
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(a);
    }

    // rematch
    @MessageMapping("/{gameId}/rematch")
    @SendTo("/topic/match/{gameId}/rematch")
    public ResponseEntity<ErrorDTO> reMatch ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId ) {
        System.out.println(gameId);
        ErrorDTO a = new ErrorDTO("emotes", "creating emotes");
        return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(a);
    }

}
