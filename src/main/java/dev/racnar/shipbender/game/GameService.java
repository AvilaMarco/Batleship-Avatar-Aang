package dev.racnar.shipbender.game;

import dev.racnar.shipbender.game.domain.Fleet;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GameStatus;
import dev.racnar.shipbender.game.dto.GameSummary;
import dev.racnar.shipbender.game.dto.GameView;
import dev.racnar.shipbender.game.event.GameEvent;
import dev.racnar.shipbender.player.dto.PlayerSummary;
import dev.racnar.shipbender.player.PlayerService;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.racnar.shipbender.game.domain.Cell;
import dev.racnar.shipbender.game.domain.TurnResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository repository;
    private final PlayerService  playerService;
    private final ApplicationEventPublisher events;

    public GameService ( GameRepository repository, PlayerService playerService, ApplicationEventPublisher events ) {
        this.repository    = repository;
        this.playerService = playerService;
        this.events        = events;
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
        events.publishEvent(new GameEvent.PlayerJoined(gameId, PlayerSummary.from(player)));
        return GameView.of(repository.save(game), player.getId());
    }

    @Transactional
    public GameView placeShips ( String email, long gameId, Fleet fleet ) {
        Player player = playerService.getPlayer(email);
        Game   game   = load(gameId);
        game.placeShips(player.getId(), fleet);
        events.publishEvent(new GameEvent.ShipsPlaced(gameId, player.getId()));
        if (game.status() == GameStatus.IN_GAME) {
            events.publishEvent(new GameEvent.GameStarted(gameId, game.getTurn()));
        }
        return GameView.of(repository.save(game), player.getId());
    }

    @Transactional
    public GameView fire ( String email, long gameId, List<String> cells ) {
        Player player = playerService.getPlayer(email);
        Game   game   = load(gameId);
        List<Cell> parsed;
        try {
            parsed = cells.stream().map(Cell::parse).toList();
        } catch (ShipbenderException e) {
            throw new ShipbenderException(ErrorCode.INVALID_SALVO, e.getMessage());
        }
        Optional<TurnResult> resolved = game.fire(player.getId(), parsed);
        events.publishEvent(new GameEvent.SalvoFired(gameId, player.getId(), resolved.map(TurnResult::turn).orElse(game.getTurn())));
        resolved.ifPresent(r -> {
            events.publishEvent(new GameEvent.TurnResolved(gameId, r.turn(), r.results().stream()
              .map(t -> new GameEvent.PlayerResult(t.playerId(), t.hits().stream().map(Cell::toString).toList(),
                t.misses().stream().map(Cell::toString).toList(), t.sunk())).toList()));
            if (r.finished()) {
                Map<String, Integer> scores = new LinkedHashMap<>();
                game.getGamePlayers().forEach(gp -> scores.put(String.valueOf(gp.getPlayer().getId()), gp.getScore()));
                events.publishEvent(new GameEvent.GameFinished(gameId, r.winnerId(), scores));
            }
        });
        return GameView.of(repository.save(game), player.getId());
    }

    private Game load ( long id ) {
        return repository.findById(id)
          .orElseThrow(() -> new ShipbenderException(ErrorCode.GAME_NOT_FOUND, "No existe la partida " + id));
    }
}
