package dev.racnar.shipbender.service.model;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.response.GameMatchDTO;
import dev.racnar.shipbender.dto.response.StatusGameDTO;
import dev.racnar.shipbender.exception.not_found.GameNotFoundException;
import dev.racnar.shipbender.models.Game;
import dev.racnar.shipbender.models.GamePlayer;
import dev.racnar.shipbender.repository.GameRepository;
import dev.racnar.shipbender.service.intereface.IGameService;
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
    public Game getGame ( Long id ) {
        return repository.findById(id)
          .orElseThrow(() -> new GameNotFoundException(id));
    }

    @Override
    public GameMatchDTO getGameMatch ( Long id, Long gamePlayerId ) {
        Game game = repository.findById(id)
          .orElseThrow(() -> new GameNotFoundException(id));
        /*ToDo: is this correct?, are you deleting the ships?*/
        List<GamePlayer> gpsOnlyWithCorrectShips = game.getGamePlayers()
          .stream()
          .peek(gp -> {
              if (gp.getId() != gamePlayerId) gp.emptyShips();
          })
          .distinct()
          .collect(Collectors.toList());
        game.setGamePlayers(gpsOnlyWithCorrectShips);
        /*ToDo: add correct scores*/
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
        return mapper.map(repository.saveAndFlush(game), GameMapDTO.class);
    }

    @Override
    public StatusGameDTO statusGame ( Long gameId ) {
        Game game = getGame(gameId);
        // ToDo: create new throw Exception
        if (game.isGameFinish()) return null;

        StatusGameDTO.StatusGameDTOBuilder builder = StatusGameDTO.builder();

        game.updateStatusGpOf(builder);
        game.updateStatusGameOf(builder);

        return builder.build();
    }
}