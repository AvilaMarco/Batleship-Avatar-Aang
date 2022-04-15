package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.exception.forbidden.PlayerDoesNotBelongGame;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GamePlayerService {

    @Autowired
    GamePlayerRepository repository;

    @Autowired
    ModelMapper mapper;

    public GamePlayerDTO save ( GamePlayer gamePlayer ) {
        return mapper.map(repository.save(gamePlayer), GamePlayerDTO.class);
    }

    public GamePlayerDTO getGamePlayerBy ( Long playerId, Long gameId ) {
        GamePlayer gamePlayer = repository
          .findByPlayerIdAndGameId(playerId, gameId)
          .orElseThrow(() -> new PlayerDoesNotBelongGame(playerId, gameId));
        return mapper.map(gamePlayer, GamePlayerDTO.class);
    }
}
