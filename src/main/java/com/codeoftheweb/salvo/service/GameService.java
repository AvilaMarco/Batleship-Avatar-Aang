package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.exception.conflict.GameAlreadyExistsException;
import com.codeoftheweb.salvo.exception.conflict.GameIsFullException;
import com.codeoftheweb.salvo.exception.conflict.PlayerAlreadyInGameException;
import com.codeoftheweb.salvo.exception.not_found.GameNotFoundException;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.service.intereface.IGameService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService implements IGameService {

    @Autowired
    GameRepository repository;

    @Autowired
    ModelMapper mapper;

    @Override
    public GameMapDTO getGame ( Long id ) {
        Game game = repository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
        return mapper.map(game, GameMapDTO.class);
    }

    @Override
    public List<GameMapDTO> getGames () {
        List<Game> games = repository.findAllByFinishDateIsNull();
        return games.stream()
          .map(g -> mapper.map(g, GameMapDTO.class))
          .collect(Collectors.toList());
    }

    @Override
    public GameMapDTO save ( Game game ) {
        return mapper.map(repository.save(game), GameMapDTO.class);
    }

    // Validations
    public void gameExists ( Long gameId ) {
        if (!repository.existsById(gameId)) {
            throw new GameNotFoundException(gameId);
        }
    }


    @Override
    public void gameNotExists ( String location ) {
        if (repository.getGameByLocation(location).isPresent())
            throw new GameAlreadyExistsException(location);
    }

    @Override
    public void gameIsNotFull ( Game game ) {
        if (game.getGamePlayers().size() == 2)
            throw new GameIsFullException(game.getId());
    }

    @Override
    public void gameNotContainsThePlayer ( Game game, Long playerId ) {
        if (game.getPlayers().stream().anyMatch(p -> p.getId() == playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }
}
