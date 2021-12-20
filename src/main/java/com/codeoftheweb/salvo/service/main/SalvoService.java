package com.codeoftheweb.salvo.service.main;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.dto.InfoGamesDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerScoreDTO;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;
import com.codeoftheweb.salvo.service.PlayerService;
import com.codeoftheweb.salvo.service.intereface.ISalvoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoService implements ISalvoService {

    @Autowired
    PlayerService playerService;

    @Autowired
    GameService gameService;

    @Autowired
    GamePlayerService gamePlayerService;

    @Override
    public InfoGamesDTO getInfoGames(Authentication authentication) {
        PlayerDTO playerDTO = playerService.getPlayer(authentication);
        List<GameDTO> games = gameService.getGames();
        /*List<GameDTO> games = null;*/
        List<PlayerScoreDTO> playersScore = playerService.getPlayersScore();
        return new InfoGamesDTO(playerDTO, games, playersScore);
    }
}
