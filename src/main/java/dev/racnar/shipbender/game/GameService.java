package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Fleet;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.dto.GameSummary;
import dev.racnar.shipbender.game.dto.GameView;
import dev.racnar.shipbender.player.PlayerService;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

    private final GameRepository repository;
    private final PlayerService  playerService;

    public GameService ( GameRepository repository, PlayerService playerService ) {
        this.repository    = repository;
        this.playerService = playerService;
    }

    @Transactional(readOnly = true)
    public List<GameSummary> list () {
        return repository.findAllByFinishDateIsNull().stream().map(GameSummary::of).toList();
    }

    @Transactional(readOnly = true)
    public GameView get ( String email, long gameId ) {
        Player player = playerService.getPlayer(email);
        return GameView.of(load(gameId), player.getId());
    }

    @Transactional
    public GameView create ( String email, NationType nation, String location ) {
        Player player = playerService.getPlayer(email);
        if (repository.existsByLocationAndFinishDateIsNull(location)) {
            throw new ShipbenderException(ErrorCode.LOCATION_TAKEN, "La ubicación " + location + " ya está ocupada");
        }
        Game game = new Game(nation, location);
        game.join(player);
        return GameView.of(repository.save(game), player.getId());
    }

    @Transactional
    public GameView join ( String email, long gameId ) {
        Player player = playerService.getPlayer(email);
        Game   game   = load(gameId);
        game.join(player);
        return GameView.of(repository.save(game), player.getId());
    }

    @Transactional
    public GameView placeShips ( String email, long gameId, Fleet fleet ) {
        Player player = playerService.getPlayer(email);
        Game   game   = load(gameId);
        game.placeShips(player.getId(), fleet);
        return GameView.of(repository.save(game), player.getId());
    }

    private Game load ( long id ) {
        return repository.findById(id)
          .orElseThrow(() -> new ShipbenderException(ErrorCode.GAME_NOT_FOUND, "No existe la partida " + id));
    }
}
