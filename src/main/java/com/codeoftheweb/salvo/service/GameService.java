package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.dto.response.StatusGameDTO;
import com.codeoftheweb.salvo.exception.conflict.GameAlreadyExistsException;
import com.codeoftheweb.salvo.exception.conflict.GameIsFullException;
import com.codeoftheweb.salvo.exception.conflict.PlayerAlreadyInGameException;
import com.codeoftheweb.salvo.exception.not_found.GameNotFoundException;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.service.intereface.IGameService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService implements IGameService {

    GameRepository repository;
    ModelMapper    mapper;

    public GameService ( GameRepository repository, ModelMapper mapper ) {
        this.repository = repository;
        this.mapper     = mapper;
    }

    @Override
    public GameMapDTO getGame ( Long id ) {
        Game game = repository.findById(id)
          .orElseThrow(() -> new GameNotFoundException(id));
        return mapper.map(game, GameMapDTO.class);
    }

    @Override
    public GameMatchDTO getGameMatch ( Long id ) {
        Game game = repository.findById(id)
          .orElseThrow(() -> new GameNotFoundException(id));
        return mapper.map(game, GameMatchDTO.class);
    }

    @Override
    public List<GameMapDTO> getGames () {
        return repository.findAllByFinishDateIsNull()
          .stream()
          .map(g -> mapper.map(g, GameMapDTO.class))
          .collect(Collectors.toList());
    }

    @Override
    public GameMapDTO save ( Game game ) {
        return mapper.map(repository.save(game), GameMapDTO.class);
    }

    public StatusGameDTO statusGame ( Long gameId ) {
        Game game = mapper.map(getGame(gameId), Game.class);
        // ToDo: create new throw Exception
        if (game.isGameFinish()) return null;

        StatusGameDTO.StatusGameDTOBuilder builder = StatusGameDTO.builder();

        game.updateStatusGpOf(builder);
        game.updateStatusGameOf(builder);

        return builder.build();
    }

    // Validations
    public void gameExists ( Long gameId ) {
        if (!repository.existsById(gameId)) {
            throw new GameNotFoundException(gameId);
        }
    }

    @Override
    public void gameNotExists ( String location ) {
        if (repository.getGameByLocation(location)
          .isPresent())
            throw new GameAlreadyExistsException(location);
    }

    @Override
    public void gameIsNotFull ( Game game ) {
        if (game.isFullGame())
            throw new GameIsFullException(game.getId());
    }

    @Override
    public void gameNotContainsThePlayer ( Game game, Long playerId ) {
        if (game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }

    @Override
    public void gameContainsThePlayer ( Long gameId, Long playerId ) {
        GameMapDTO gameDTO = getGame(gameId);
        Game       game    = mapper.map(gameDTO, Game.class);
        if (!game.containsPlayer(playerId))
            throw new PlayerAlreadyInGameException(playerId);
    }
}
