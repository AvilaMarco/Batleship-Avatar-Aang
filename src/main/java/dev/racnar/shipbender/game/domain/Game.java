package dev.racnar.shipbender.game.domain;

import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Game {

    private static final int SALVO_SIZE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private NationType nation;
    private String     location;
    private Integer    turn;

    @CreationTimestamp
    private LocalDateTime created;

    private LocalDateTime finishDate;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GamePlayer> gamePlayers = new ArrayList<>();

    public Game ( NationType nation, String location ) {
        this.nation   = nation;
        this.location = location;
        this.turn     = 1;
    }

    /* PUBLIC METHODS */

    public GamePlayer join ( Player player ) {
        if (containsPlayer(player.getId())) {
            throw new ShipbenderException(ErrorCode.ALREADY_IN_GAME, "ya estás en la partida " + id);
        }
        if (isFullGame()) {
            throw new ShipbenderException(ErrorCode.GAME_FULL, "La partida " + id + " está completa");
        }
        GamePlayer gp = new GamePlayer(player, this);
        gamePlayers.add(gp);
        return gp;
    }

    public void placeShips ( long playerId, Fleet fleet ) {
        GamePlayer gp = participant(playerId);
        if (gp.hasShips()) {
            throw new ShipbenderException(ErrorCode.SHIPS_ALREADY_PLACED, "ya colocaste tus barcos");
        }
        for (ShipPlacement p : fleet.ships()) {
            Ship ship = new Ship(p.type(), p.cells().stream().map(Cell::toString).toList());
            gp.getShips().add(ship);
        }
    }


    /** Turnos simultáneos: resuelve cuando ambos dispararon el turno actual (ADR 004 Anexo F). */
    public Optional<TurnResult> fire ( long playerId, List<Cell> cells ) {
        GamePlayer me = participant(playerId);
        if (status() != GameStatus.IN_GAME) {
            throw new ShipbenderException(ErrorCode.GAME_NOT_IN_PROGRESS, "la partida " + id + " no está en juego");
        }
        if (me.firedIn(turn)) {
            throw new ShipbenderException(ErrorCode.SALVO_ALREADY_FIRED, "ya disparaste en el turno " + turn);
        }
        Set<String> previous = me.firedCells();
        List<String> locations = cells.stream().map(Cell::toString).toList();
        if (locations.size() != SALVO_SIZE || new HashSet<>(locations).size() != SALVO_SIZE
          || locations.stream().anyMatch(previous::contains)) {
            throw new ShipbenderException(ErrorCode.INVALID_SALVO,
              "se requieren " + SALVO_SIZE + " celdas distintas y no disparadas antes");
        }
        me.addSalvo(new Salvo(turn, locations));

        if (!gamePlayers.stream().allMatch(gp -> gp.firedIn(turn))) {
            return Optional.empty();
        }
        List<TurnResult.PlayerTurn> results = gamePlayers.stream().map(this::resolve).toList();
        // won[i]: i hundió toda la flota del rival (aunque el rival ya lo haya hecho en este mismo turno)
        boolean[] won = new boolean[2];
        for (int i = 0; i < 2; i++) {
            won[i] = gamePlayers.get(1 - i).allSunk(gamePlayers.get(i));
        }
        int resolvedTurn = turn;
        if (!won[0] && !won[1]) {
            turn++;
            return Optional.of(new TurnResult(resolvedTurn, results, false, null));
        }
        boolean draw = won[0] && won[1];
        for (int i = 0; i < 2; i++) {
            gamePlayers.get(i).setScore(draw ? 1 : won[i] ? 3 : 0);
        }
        finishDate();
        Long winner = draw ? null : gamePlayers.get(won[0] ? 0 : 1).getPlayer().getId();
        return Optional.of(new TurnResult(resolvedTurn, results, true, winner));
    }

    private TurnResult.PlayerTurn resolve ( GamePlayer shooter ) {
        GamePlayer target = opponentOf(shooter.getPlayer().getId()).orElseThrow();
        Set<String> all = shooter.firedCells();
        Set<String> before = new HashSet<>(all);
        List<String> now = shooter.salvoOf(turn).getLocations();
        before.removeAll(now);
        List<Cell> hits = now.stream().filter(target::hasShipAt).map(Cell::parse).toList();
        List<Cell> misses = now.stream().filter(c -> !target.hasShipAt(c)).map(Cell::parse).toList();
        List<ShipType> sunk = target.getShips().stream()
          .filter(s -> all.containsAll(s.getLocations()) && !before.containsAll(s.getLocations()))
          .map(Ship::getType).sorted().toList();
        return new TurnResult.PlayerTurn(shooter.getPlayer().getId(), hits, misses, sunk);
    }

    public GamePlayer participant ( long playerId ) {
        return gamePlayers.stream()
          .filter(gp -> gp.getPlayer().getId() == playerId)
          .findFirst()
          .orElseThrow(() -> new ShipbenderException(ErrorCode.NOT_A_PARTICIPANT, "no participas en la partida " + id));
    }

    public Optional<GamePlayer> opponentOf ( long playerId ) {
        return gamePlayers.stream()
          .filter(gp -> gp.getPlayer().getId() != playerId)
          .findFirst();
    }

    public GameStatus status () {
        if (finishDate != null) {
            return GameStatus.FINISH;
        }
        if (gamePlayers.size() < 2 || !gamePlayers.stream().allMatch(GamePlayer::hasShips)) {
            return GameStatus.CREATED;
        }
        return GameStatus.IN_GAME;
    }

    public void finishDate () {
        this.finishDate = LocalDateTime.now();
    }

    public boolean containsPlayer ( Long playerId ) {
        return gamePlayers.stream()
          .map(GamePlayer::getPlayer)
          .anyMatch(p -> p.getId() == playerId);
    }

    public boolean isGameFinish () {
        return gamePlayers.stream()
          .allMatch(gp -> gp.getScore() != null);
    }

    public boolean isFullGame () {
        return gamePlayers.size() == 2;
    }
}
