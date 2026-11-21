package dev.racnar.shipbender.game;

import dev.racnar.shipbender.shared.error.forbidden.PlayerDoesNotBelongGame;
import dev.racnar.shipbender.game.domain.GamePlayer;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamePlayerService {

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
