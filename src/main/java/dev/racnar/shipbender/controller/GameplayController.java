package dev.racnar.shipbender.controller;

import dev.racnar.shipbender.dto.request.websocket.ShipDTO;
import dev.racnar.shipbender.dto.response.GameDataDTO;
import dev.racnar.shipbender.dto.response.UserEmoteDTO;
import dev.racnar.shipbender.service.intereface.IGameplayService;
import dev.racnar.shipbender.service.intereface.IMatchService;
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

}
