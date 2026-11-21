package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.Cell;
import dev.racnar.shipbender.game.domain.Fleet;
import dev.racnar.shipbender.game.domain.ShipPlacement;
import dev.racnar.shipbender.game.domain.ShipType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceShipsRequest(@NotNull List<ShipRequest> ships) {

    public record ShipRequest(ShipType type, List<String> cells) {}

    /** Parsea las celdas; el dominio (Fleet) valida el resto. */
    public Fleet toFleet () {
        return new Fleet(ships.stream()
          .map(s -> s == null ? null : new ShipPlacement(s.type(), s.cells() == null ? null : s.cells().stream().map(Cell::parse).toList()))
          .toList());
    }
}
