package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.dto.ShipDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.dto.response.StatusGameDTO;
import com.codeoftheweb.salvo.service.intereface.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GameMatchDTO statusGame ( @PathVariable Long game_id, Authentication authentication ) {
       return matchService.viewMatch(authentication, game_id);
    }

    /* WEB SOCKETS */

    // enviar barcos
    @MessageMapping("/{gameId}")
    @SendTo("/topic/match/{gameId}/ships")
    public StatusGameDTO matchShips( @DestinationVariable Long gameId, @RequestBody List<ShipDTO> ships, Authentication authentication){
        System.out.println(ships);
        System.out.println(gameId);
        return new StatusGameDTO("WAITING", 2L, null);
    }

    // unirse a la partida
    /*@MessageMapping("/{gameId}")*/
/*    @SendTo("/topic/match/{gameId}")
    public GamePlayerDTO match ( @DestinationVariable Long gameId, Authentication authentication ) {
        return matchService.viewMatch(authentication, gameId);
    }*/

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
