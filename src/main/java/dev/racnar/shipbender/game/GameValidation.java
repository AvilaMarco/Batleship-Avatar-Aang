package dev.racnar.shipbender.game;

import dev.racnar.shipbender.shared.error.conflict.GameAlreadyExistsException;
import dev.racnar.shipbender.shared.error.conflict.GameIsFullException;
import dev.racnar.shipbender.shared.error.conflict.PlayerAlreadyInGameException;
import dev.racnar.shipbender.shared.error.not_found.GameNotFoundException;
import dev.racnar.shipbender.game.domain.Game;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class GameValidation {

    GameRepository repository;
    ModelMapper    mapper;

    public GameValidation ( GameRepository repository, ModelMapper mapper ) {
        this.repository = repository;
        this.mapper     = mapper;
    }

    public void exists ( Long gameId ) {
        if (!repository.existsById(gameId)) {
            throw new GameNotFoundException(gameId);
        }
    }

    public void notExists ( String location ) {
        if (repository.getGameByLocation(location)
          .isPresent())
            throw new GameAlreadyExistsException(location);
    }

    public void isNotFull ( Game game ) {
        if (game.isFullGame())
            throw new GameIsFullException(game.getId());
    }

    public void notContainsThePlayer ( Game game, Long playerId ) {
        if (game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }

    public void containsThePlayer ( Long gameId, Long playerId ) {
        Game game = repository.findById(gameId)
          .orElseThrow(() -> new GameNotFoundException(gameId));
        if (!game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }
}
