package dev.racnar.shipbender.unit.domain;

import dev.racnar.shipbender.game.domain.*;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static dev.racnar.shipbender.game.domain.ShipType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Reglas de salvos y fin de partida (ADR 004 Anexo F, tarea 411). Dominio puro: sin Spring ni Docker. */
class SalvoRulesTest {

    // La flota de ambos jugadores ocupa A1-A5, B1-B4, C1-C3, D1-D3, E1-E2 (17 celdas).
    private static final List<String[]> SINK_ALL = List.of(
      new String[]{"A1", "A2", "A3", "A4", "A5"},   // hunde CARRIER
      new String[]{"B1", "B2", "B3", "B4", "C1"},   // hunde BATTLESHIP
      new String[]{"C2", "C3", "D1", "D2", "D3"},   // hunde SUBMARINE y DESTROYER
      new String[]{"E1", "E2", "F1", "F2", "F3"});  // hunde PATROL_BOAT
    private static final List<String[]> ALL_MISSES = List.of(
      new String[]{"G1", "G2", "G3", "G4", "G5"},
      new String[]{"G6", "G7", "G8", "G9", "G10"},
      new String[]{"H1", "H2", "H3", "H4", "H5"},
      new String[]{"H6", "H7", "H8", "H9", "H10"});

    private Game game;

    @BeforeEach
    void setUp () {
        game = new Game(null, "Ba Sing Se");
        game.join(player(1));
    }

    private static Player player ( long id ) {
        Player p = new Player();
        p.setId(id);
        return p;
    }

    private static ShipPlacement ship ( ShipType type, String... cells ) {
        return new ShipPlacement(type, cells(cells));
    }

    private static List<Cell> cells ( String... cells ) {
        return Arrays.stream(cells).map(Cell::parse).toList();
    }

    private void startGame () {
        game.join(player(2));
        Fleet fleet = new Fleet(List.of(
          ship(CARRIER, "A1", "A2", "A3", "A4", "A5"),
          ship(BATTLESHIP, "B1", "B2", "B3", "B4"),
          ship(SUBMARINE, "C1", "C2", "C3"),
          ship(DESTROYER, "D1", "D2", "D3"),
          ship(PATROL_BOAT, "E1", "E2")));
        game.placeShips(1, fleet);
        game.placeShips(2, fleet);
    }

    private static void assertCode ( ErrorCode code, Executable action ) {
        assertThatThrownBy(action::execute)
          .isInstanceOfSatisfying(ShipbenderException.class, e -> assertThat(e.code()).isEqualTo(code));
    }

    /** Ambos disparan el turno actual con las celdas dadas; devuelve el resultado del segundo disparo. */
    private TurnResult bothFire ( String[] first, String[] second ) {
        game.fire(1, cells(first));
        return game.fire(2, cells(second)).orElseThrow();
    }

    @Test
    void fire_beforeGameIsInGame_throwsGameNotInProgress () {
        assertCode(ErrorCode.GAME_NOT_IN_PROGRESS, () -> game.fire(1, cells("A1", "A2", "A3", "A4", "A5")));
    }

    @Test
    void fire_twiceInSameTurn_throwsSalvoAlreadyFired () {
        startGame();
        game.fire(1, cells("F1", "F2", "F3", "F4", "F5"));

        assertCode(ErrorCode.SALVO_ALREADY_FIRED, () -> game.fire(1, cells("G1", "G2", "G3", "G4", "G5")));
    }

    @Test
    void fire_withFourCells_throwsInvalidSalvo () {
        startGame();
        assertCode(ErrorCode.INVALID_SALVO, () -> game.fire(1, cells("F1", "F2", "F3", "F4")));
    }

    @Test
    void fire_withSixCells_throwsInvalidSalvo () {
        startGame();
        assertCode(ErrorCode.INVALID_SALVO, () -> game.fire(1, cells("F1", "F2", "F3", "F4", "F5", "F6")));
    }

    @Test
    void fire_withRepeatedCells_throwsInvalidSalvo () {
        startGame();
        assertCode(ErrorCode.INVALID_SALVO, () -> game.fire(1, cells("F1", "F1", "F3", "F4", "F5")));
    }

    @Test
    void fire_atCellAlreadyShotInPreviousTurn_throwsInvalidSalvo () {
        startGame();
        bothFire(new String[]{"F1", "F2", "F3", "F4", "F5"}, new String[]{"G1", "G2", "G3", "G4", "G5"});

        assertCode(ErrorCode.INVALID_SALVO, () -> game.fire(1, cells("F1", "H1", "H2", "H3", "H4")));
    }

    @Test
    void fire_firstOfTheTurn_returnsEmptyAndKeepsTurn () {
        startGame();

        assertThat(game.fire(1, cells("F1", "F2", "F3", "F4", "F5"))).isEmpty();
        assertThat(game.getTurn()).isEqualTo(1);
    }

    @Test
    void fire_secondOfTheTurn_resolvesHitsAndMissesPerPlayerAndAdvancesTurn () {
        startGame();

        TurnResult r = bothFire(new String[]{"A1", "A2", "F1", "F2", "F3"}, new String[]{"E1", "G1", "G2", "G3", "G4"});

        assertThat(r.turn()).isEqualTo(1);
        assertThat(r.finished()).isFalse();
        assertThat(r.winnerId()).isNull();
        assertThat(game.getTurn()).isEqualTo(2);
        TurnResult.PlayerTurn host = r.results().stream().filter(p -> p.playerId() == 1).findFirst().orElseThrow();
        TurnResult.PlayerTurn guest = r.results().stream().filter(p -> p.playerId() == 2).findFirst().orElseThrow();
        assertThat(host.hits()).containsExactlyInAnyOrderElementsOf(cells("A1", "A2"));
        assertThat(host.misses()).containsExactlyInAnyOrderElementsOf(cells("F1", "F2", "F3"));
        assertThat(host.sunk()).isEmpty();
        assertThat(guest.hits()).containsExactly(Cell.parse("E1"));
        assertThat(guest.misses()).hasSize(4);
    }

    @Test
    void fire_sinkingWholeFleet_finishesGameWithWinner3AndLoser0 () {
        startGame();
        TurnResult last = null;
        for (int t = 0; t < 4; t++) {
            last = bothFire(SINK_ALL.get(t), ALL_MISSES.get(t));
            if (t == 2) {
                // sunk cuenta solo los barcos hundidos en ese turno, no el acumulado
                assertThat(last.results().get(0).sunk()).containsExactlyInAnyOrder(SUBMARINE, DESTROYER);
            }
        }

        assertThat(last.finished()).isTrue();
        assertThat(last.winnerId()).isEqualTo(1L);
        assertThat(game.status()).isEqualTo(GameStatus.FINISH);
        assertThat(game.participant(1).getScore()).isEqualTo(3);
        assertThat(game.participant(2).getScore()).isEqualTo(0);
    }

    @Test
    void fire_bothFleetsSunkInSameTurn_isDrawWithOnePointEachAndNoWinner () {
        startGame();
        TurnResult last = null;
        for (String[] salvo : SINK_ALL) {
            last = bothFire(salvo, salvo);
        }

        assertThat(last.finished()).isTrue();
        assertThat(last.winnerId()).isNull();
        assertThat(game.participant(1).getScore()).isEqualTo(1);
        assertThat(game.participant(2).getScore()).isEqualTo(1);
    }

    @Test
    void fire_oneFleetSunkButRivalHasNotFired_gameStaysOpen () {
        startGame();
        for (int t = 0; t < 3; t++) {
            bothFire(SINK_ALL.get(t), ALL_MISSES.get(t));
        }

        // la victoria solo se evalua al resolver el turno: el anfitrion hunde la ultima nave pero el rival aun no dispara
        assertThat(game.fire(1, cells(SINK_ALL.get(3)))).isEmpty();
        assertThat(game.status()).isEqualTo(GameStatus.IN_GAME);
        assertThat(game.participant(1).getScore()).isNull();
    }
}
