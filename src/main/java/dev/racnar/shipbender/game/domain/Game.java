package dev.racnar.shipbender.game.domain;

import dev.racnar.shipbender.game.dto.StatusGameDTO;
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
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Game {

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

    public void updateStatusGpOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        if (!this.isGameWaitingPlayer()) {
            if (this.isClientHasShips()) {
                builder.client(PlayerStatus.WITH_SHIPS);
            } else {
                builder.client(PlayerStatus.WITHOUT_SHIPS);
            }
        } else {
            builder.client(PlayerStatus.WAITING);
        }
        if (this.isHostHasShips()) {
            builder.host(PlayerStatus.WITH_SHIPS);
        } else {
            builder.host(PlayerStatus.WITHOUT_SHIPS);
        }
    }

    public void updateStatusGameOf ( StatusGameDTO.StatusGameDTOBuilder builder ) {
        if (this.isGameWaitingPlayer() || this.isGameWaitingShips()) {
            builder.game(GameStatus.CREATED);
        } else if (this.isGameFinish()) {
            builder.game(GameStatus.FINISH);
        } else {
            builder.game(GameStatus.IN_GAME);
        }
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

    /* UTILS */
    private boolean isGameWaitingPlayer () {
        return gamePlayers.size() == 1;
    }

    private boolean isGameWaitingShips () {
        return !gamePlayers.stream()
          .allMatch(GamePlayer::hasShips);
    }

    private boolean isClientHasShips () {
        return gamePlayers.get(1)
          .hasShips();
    }

    private boolean isHostHasShips () {
        return gamePlayers.get(0)
          .hasShips();
    }
}
