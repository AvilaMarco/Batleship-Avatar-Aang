package dev.racnar.shipbender.unit.domain;

import dev.racnar.shipbender.game.domain.Cell;
import dev.racnar.shipbender.shared.error.ErrorCode;
import dev.racnar.shipbender.shared.error.ShipbenderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CellTest {

    @ParameterizedTest
    @ValueSource(strings = {"A1", "J10", "B7"})
    void parse_validCell_roundTripsThroughToString ( String text ) {
        assertThat(Cell.parse(text).toString()).isEqualTo(text);
    }

    @Test
    void parse_J10_hasRowAndColumn () {
        Cell cell = Cell.parse("J10");
        assertThat(cell.row()).isEqualTo('J');
        assertThat(cell.col()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"K1", "A0", "A11", "a1", "1A", ""})
    void parse_invalidCell_throwsInvalidFleet ( String text ) {
        assertThatThrownBy(() -> Cell.parse(text))
          .isInstanceOfSatisfying(ShipbenderException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_FLEET));
    }

    @Test
    void parse_null_throwsInvalidFleet () {
        assertThatThrownBy(() -> Cell.parse(null))
          .isInstanceOfSatisfying(ShipbenderException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_FLEET));
    }
}
