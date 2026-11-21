package dev.racnar.shipbender.unit.domain;

import dev.racnar.shipbender.game.domain.*;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static dev.racnar.shipbender.game.domain.ShipType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private Game   game;
    private Player host;
    private Player guest;

    @BeforeEach
    void setUp () {
        game = new Game(null, "Ba Sing Se");
        host = player(1);
        guest = player(2);
        game.join(host);
    }

    private static Player player ( long id ) {
        Player p = new Player();
        p.setId(id);
        return p;
    }

    private static ShipPlacement ship ( ShipType type, String... cells ) {
        return new ShipPlacement(type, Arrays.stream(cells).map(Cell::parse).toList());
    }

    private static Fleet fleet () {
        return new Fleet(List.of(
          ship(CARRIER, "A1", "A2", "A3", "A4", "A5"),
          ship(BATTLESHIP, "B1", "B2", "B3", "B4"),
          ship(SUBMARINE, "C1", "C2", "C3"),
          ship(DESTROYER, "D1", "D2", "D3"),
          ship(PATROL_BOAT, "E1", "E2")));
    }

    private static void assertCode ( ErrorCode code, Executable action ) {
        assertThatThrownBy(action::execute)
          .isInstanceOfSatisfying(ShipbenderException.class, e -> assertThat(e.code()).isEqualTo(code));
    }

    @Test
    void status_withOnePlayer_isCreatedAndHasNoOpponent () {
        assertThat(game.status()).isEqualTo(GameStatus.CREATED);
        assertThat(game.opponentOf(1)).isEmpty();
    }

    @Test
    void join_secondPlayer_isAddedAndBecomesOpponent () {
        GamePlayer gp = game.join(guest);

        assertThat(game.getGamePlayers()).hasSize(2).contains(gp);
        assertThat(game.opponentOf(1)).contains(gp);
    }

    @Test
    void join_thirdPlayer_throwsGameFull () {
        game.join(guest);
        assertCode(ErrorCode.GAME_FULL, () -> game.join(player(3)));
    }

    @Test
    void join_samePlayerTwice_throwsAlreadyInGame () {
        assertCode(ErrorCode.ALREADY_IN_GAME, () -> game.join(host));
    }

    @Test
    void participant_ofStranger_throwsNotAParticipant () {
        assertCode(ErrorCode.NOT_A_PARTICIPANT, () -> game.participant(99));
    }

    @Test
    void placeShips_byStranger_throwsNotAParticipant () {
        assertCode(ErrorCode.NOT_A_PARTICIPANT, () -> game.placeShips(99, fleet()));
    }

    @Test
    void placeShips_twice_throwsShipsAlreadyPlaced () {
        game.placeShips(1, fleet());
        assertCode(ErrorCode.SHIPS_ALREADY_PLACED, () -> game.placeShips(1, fleet()));
    }

    @Test
    void placeShips_createsFiveShips () {
        game.placeShips(1, fleet());
        assertThat(game.participant(1).getShips()).hasSize(5);
    }

    @Test
    void status_onlyOnePlayerPlacedShips_isStillCreated () {
        game.join(guest);
        game.placeShips(1, fleet());
        assertThat(game.status()).isEqualTo(GameStatus.CREATED);
    }

    @Test
    void status_bothPlacedShips_isInGame () {
        game.join(guest);
        game.placeShips(1, fleet());
        game.placeShips(2, fleet());
        assertThat(game.status()).isEqualTo(GameStatus.IN_GAME);
    }

    @Test
    void status_withFinishDate_isFinish () {
        game.setFinishDate(LocalDateTime.now());
        assertThat(game.status()).isEqualTo(GameStatus.FINISH);
    }

    @Test
    void participant_withIdsAbove127_comparesByValueNotByReference () {
        Game g = new Game(null, "x");
        g.join(player(1000));
        g.join(player(2000));

        assertThat(g.participant(1000).getPlayer().getId()).isEqualTo(1000);
        assertThat(g.opponentOf(1000).orElseThrow().getPlayer().getId()).isEqualTo(2000);
        assertCode(ErrorCode.ALREADY_IN_GAME, () -> g.join(player(2000)));
    }
}
