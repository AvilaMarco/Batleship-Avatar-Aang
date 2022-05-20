package com.codeblockacademy.shipbender.service;

import com.codeblockacademy.shipbender.dto.GamePlayerDTO;
import com.codeblockacademy.shipbender.exception.forbidden.PlayerDoesNotBelongGame;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.repository.GamePlayerRepository;
import com.codeblockacademy.shipbender.service.intereface.IGamePlayerService;
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
