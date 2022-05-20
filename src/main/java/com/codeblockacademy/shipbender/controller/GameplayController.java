package com.codeblockacademy.shipbender.controller;

import com.codeblockacademy.shipbender.dto.error.ErrorDTO;
import com.codeblockacademy.shipbender.dto.request.websocket.EmoteDTO;
import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.service.intereface.IGameplayService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gameplay")
public class GameplayController {

    IGameplayService gameplayService;

    public GameplayController ( IGameplayService gameplayService ) {
        this.gameplayService = gameplayService;
    }

    /* WEB SOCKETS */

    // enviar barcos
    @MessageMapping("/{gameId}/ships")
    @SendTo("/topic/gameplay/{gameId}/ships")
    public StatusGameDTO matchShips ( Authentication authentication, @DestinationVariable Long gameId, @RequestBody List<ShipDTO> ships ) {
        return gameplayService.createShips(authentication, gameId, ships);
    }

    // enviar emotes
    @MessageMapping("/{gameId}/emotes/{userId}") // entrada
    @SendTo("/topic/gameplay/{gameId}/emotes/{userId}") // salida
    public ErrorDTO matchEmotes ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId, @RequestBody EmoteDTO emote ) {
        System.out.println(gameId);
        System.out.println(emote);
        return new ErrorDTO("emotes", "creating emotes" + userId);
    }

    // enviar disparos
    @MessageMapping("/{gameId}/salvos")
    @SendTo("/topic/gameplay/{gameId}/salvos")
    public ErrorDTO matchSalvos ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId ) {
        System.out.println(gameId);
        ErrorDTO a = new ErrorDTO("emotes", "creating emotes");
        return a;
    }

    // rematch
    @MessageMapping("/{gameId}/rematch")
    @SendTo("/topic/gameplay/{gameId}/rematch")
    public ErrorDTO reMatch ( Authentication authentication, @DestinationVariable Long gameId, @DestinationVariable Long userId ) {
        System.out.println(gameId);
        ErrorDTO a = new ErrorDTO("emotes", "creating emotes");
        return a;
    }

}
