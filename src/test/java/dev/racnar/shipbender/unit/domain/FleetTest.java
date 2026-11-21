package dev.racnar.shipbender.unit.domain;

import dev.racnar.shipbender.game.domain.Cell;
import dev.racnar.shipbender.game.domain.Fleet;
import dev.racnar.shipbender.game.domain.ShipPlacement;
import dev.racnar.shipbender.game.domain.ShipType;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.racnar.shipbender.game.domain.ShipType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetTest {

    private static ShipPlacement ship ( ShipType type, String... cells ) {
        return new ShipPlacement(type, Arrays.stream(cells).map(Cell::parse).toList());
    }

    /** Flota válida: filas A..E horizontales, columnas 1..n. */
    private static List<ShipPlacement> validShips () {
        return new ArrayList<>(List.of(
          ship(CARRIER, "A1", "A2", "A3", "A4", "A5"),
          ship(BATTLESHIP, "B1", "B2", "B3", "B4"),
          ship(SUBMARINE, "C1", "C2", "C3"),
          ship(DESTROYER, "D1", "D2", "D3"),
          ship(PATROL_BOAT, "E1", "E2")));
    }

    private static void assertInvalid ( List<ShipPlacement> ships ) {
        assertThatThrownBy(() -> new Fleet(ships))
          .isInstanceOfSatisfying(ShipbenderException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_FLEET));
    }

    @Test
    void fleet_classicHorizontalFleet_isValid () {
        assertThatCode(() -> new Fleet(validShips())).doesNotThrowAnyException();
    }

    @Test
    void fleet_verticalShipWithUnorderedCells_isValid () {
        List<ShipPlacement> ships = validShips();
        ships.set(0, ship(CARRIER, "H10", "F10", "J10", "G10", "I10"));
        assertThatCode(() -> new Fleet(ships)).doesNotThrowAnyException();
    }

    @Test
    void fleet_missingShipType_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.remove(4);
        assertInvalid(ships);
    }

    @Test
    void fleet_repeatedShipType_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.set(4, ship(DESTROYER, "E1", "E2", "E3"));
        assertInvalid(ships);
    }

    @Test
    void fleet_wrongSize_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.set(0, ship(CARRIER, "A1", "A2", "A3", "A4"));
        assertInvalid(ships);
    }

    @Test
    void fleet_nonConsecutiveCells_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.set(4, ship(PATROL_BOAT, "E1", "E3"));
        assertInvalid(ships);
    }

    @Test
    void fleet_diagonalShip_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.set(4, ship(PATROL_BOAT, "E1", "F2"));
        assertInvalid(ships);
    }

    @Test
    void fleet_overlappingShips_isInvalid () {
        List<ShipPlacement> ships = validShips();
        ships.set(4, ship(PATROL_BOAT, "A1", "B1"));
        assertInvalid(ships);
    }
}
