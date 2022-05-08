package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.exception.forbidden.PlayerDoesNotBelongGame;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.service.intereface.IGamePlayerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class GamePlayerService implements IGamePlayerService {

    GamePlayerRepository repository;
    ModelMapper          mapper;

    public GamePlayerService ( GamePlayerRepository repository, ModelMapper mapper ) {
        this.repository = repository;
        this.mapper     = mapper;
    }

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
