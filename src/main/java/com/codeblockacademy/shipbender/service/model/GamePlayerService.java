package com.codeblockacademy.shipbender.service.model;

import com.codeblockacademy.shipbender.exception.forbidden.PlayerDoesNotBelongGame;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.repository.GamePlayerRepository;
import com.codeblockacademy.shipbender.service.intereface.IGamePlayerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamePlayerService implements IGamePlayerService {

    GamePlayerRepository repository;
    ModelMapper          mapper;

    public GamePlayerService ( GamePlayerRepository repository, ModelMapper mapper ) {
        this.repository = repository;
        this.mapper     = mapper;
    }

    @Transactional
    public GamePlayer save ( GamePlayer gamePlayer ) {
        return repository.saveAndFlush(gamePlayer);
    }

    public GamePlayer getGamePlayerBy ( Long playerId, Long gameId ) {
        return repository
          .findByPlayerIdAndGameId(playerId, gameId)
          .orElseThrow(() -> new PlayerDoesNotBelongGame(playerId, gameId));
    }
}
