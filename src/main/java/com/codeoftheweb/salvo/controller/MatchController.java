package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.service.intereface.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchController {

    @Autowired
    IMatchService matchService;

    /* WEB SOCKETS */

    // unirse a la partida
    @MessageMapping("/{gameId}")
    @SendTo("/topic/match/{gameId}")
    public GamePlayerDTO match ( @DestinationVariable Long gameId, Authentication authentication ) {
        return matchService.viewMatch(authentication, gameId);
    }

    /* // enviar barcos
     @MessageMapping("/{gameId}")
     @SendTo("/topic/match/{gameId}/ships")
     public GamePlayerDTO matchShips(@DestinationVariable Long gameId, Authentication authentication){
         return matchService.viewMatch(authentication, gameId);
     }

     // enviar emotes
     @MessageMapping("/{gameId}")
     @SendTo("/topic/match/{gameId}")
     public GamePlayerDTO matchEmotes(@DestinationVariable Long gameId, Authentication authentication){
         return matchService.viewMatch(authentication, gameId);
     }

     // enviar disparos
     @MessageMapping("/{gameId}")
     @SendTo("/topic/match/{gameId}")
     public GamePlayerDTO matchSalvos(@DestinationVariable Long gameId, Authentication authentication){
         return matchService.viewMatch(authentication, gameId);
     }

     // rematch
     @MessageMapping("/{gameId}")
     @SendTo("/topic/match/{gameId}")
     public GamePlayerDTO reMatch(@DestinationVariable Long gameId, Authentication authentication){
         return matchService.viewMatch(authentication, gameId);
     }
 */
}
