package com.codeblockacademy.shipbender.service.validations;

import com.codeblockacademy.shipbender.exception.conflict.GameAlreadyExistsException;
import com.codeblockacademy.shipbender.exception.conflict.GameIsFullException;
import com.codeblockacademy.shipbender.exception.conflict.PlayerAlreadyInGameException;
import com.codeblockacademy.shipbender.exception.not_found.GameNotFoundException;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.repository.GameRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class GameValidation implements IGameValidation {

    GameRepository repository;
    ModelMapper    mapper;

    public GameValidation ( GameRepository repository, ModelMapper mapper ) {
        this.repository = repository;
        this.mapper     = mapper;
    }

    @Override
    public void exists ( Long gameId ) {
        if (!repository.existsById(gameId)) {
            throw new GameNotFoundException(gameId);
        }
    }

    @Override
    public void notExists ( String location ) {
        if (repository.getGameByLocation(location)
          .isPresent())
            throw new GameAlreadyExistsException(location);
    }

    @Override
    public void isNotFull ( Game game ) {
        if (game.isFullGame())
            throw new GameIsFullException(game.getId());
    }

    @Override
    public void notContainsThePlayer ( Game game, Long playerId ) {
        if (game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }

    @Override
    public void containsThePlayer ( Long gameId, Long playerId ) {
        this.exists(gameId);
        Game game = repository.getById(gameId);
        if (!game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }
}
