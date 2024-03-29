package com.codeblockacademy.shipbender.controller;

import com.codeblockacademy.shipbender.dto.error.ErrorDTO;
import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.GameDataDTO;
import com.codeblockacademy.shipbender.dto.response.UserEmoteDTO;
import com.codeblockacademy.shipbender.service.intereface.IGameplayService;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameplayController {

    IGameplayService gameplayService;
    IMatchService    matchService;

    public GameplayController ( IGameplayService gameplayService, IMatchService matchService ) {
        this.gameplayService = gameplayService;
        this.matchService    = matchService;
    }

    /* WEB SOCKETS */

    // enviar barcos
    @MessageMapping("/{gameId}/ships")
    @SendTo("/topic/gameplay/{gameId}/ships")
    public GameDataDTO matchShips ( @DestinationVariable Long gameId, @RequestBody List<ShipDTO> ships, Authentication authentication ) {
        return gameplayService.createShips(authentication, gameId, ships);
    }

    // enviar emotes
    @MessageMapping("/{gameId}/emotes") // entrada
    @SendTo("/topic/gameplay/{gameId}/emotes") // salida
    public UserEmoteDTO matchEmotes ( @DestinationVariable Long gameId, @RequestBody String emote ) {
        return gameplayService.sendEmote(gameId, emote);
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
